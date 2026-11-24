package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Set;

import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.OperationResult;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.port.UserRepository;
import ru.mifi.oop.finance.core.port.WalletRepository;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Transfers money between two registered users. Both wallets are updated and persisted,
 * so the operation is visible to the recipient immediately after their next login.
 */
public final class TransferService {

    public static final String OUTGOING_CATEGORY = "Переводы (исходящие)";
    public static final String INCOMING_CATEGORY = "Переводы (входящие)";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public TransferService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            NotificationService notificationService,
            Clock clock) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public TransferResult transfer(Wallet sender, String rawRecipientLogin, BigDecimal amount, String rawNote) {
        String recipientLogin = Validators.login(rawRecipientLogin);
        String note = Validators.note(rawNote);
        BigDecimal normalizedAmount = Money.positive(amount, "Сумма перевода");
        if (sender.ownerLogin().equals(recipientLogin)) {
            throw new DomainException("Нельзя перевести деньги самому себе.");
        }

        UserAccount recipientUser = userRepository.findByLogin(recipientLogin)
                .orElseThrow(() -> new DomainException("Получатель «" + recipientLogin
                        + "» не найден. Пользователь должен быть зарегистрирован."));

        if (sender.balance().compareTo(normalizedAmount) < 0) {
            throw new DomainException("Недостаточно средств для перевода: доступно "
                    + Money.format(sender.balance()) + ", требуется " + Money.format(normalizedAmount) + ".");
        }

        Wallet recipientWallet = walletRepository.findByOwner(recipientUser.login())
                .orElseGet(() -> new Wallet(recipientUser.login(), clock.instant()));

        String outgoingNote = Validators.note("Перевод пользователю " + recipientLogin
                + (note.isEmpty() ? "" : ": " + note));
        String incomingNote = Validators.note("Перевод от " + sender.ownerLogin()
                + (note.isEmpty() ? "" : ": " + note));

        // Preflight both wallets before mutation: a conflict on the recipient side
        // must not leave a half-completed transfer in the sender wallet.
        ensureCategoryType(sender, OUTGOING_CATEGORY, CategoryType.EXPENSE);
        ensureCategoryType(recipientWallet, INCOMING_CATEGORY, CategoryType.INCOME);

        String outgoingCategoryBefore = sender.categories().find(OUTGOING_CATEGORY)
                .map(item -> item.name())
                .orElse(OUTGOING_CATEGORY);
        BigDecimal spentBefore = sender.ledger().total(TransactionType.EXPENSE,
                Set.of(outgoingCategoryBefore), DateRange.all());
        BigDecimal expensesBefore = sender.totalExpense(DateRange.all());
        BigDecimal balanceBefore = sender.balance();

        OperationResult outgoing = sender.addExpense(
                normalizedAmount, OUTGOING_CATEGORY, outgoingNote, clock.instant());
        OperationResult incoming = recipientWallet.addIncome(
                normalizedAmount, INCOMING_CATEGORY, incomingNote, clock.instant());

        walletRepository.save(sender);
        walletRepository.save(recipientWallet);

        List<Notification> notifications = notificationService.afterExpense(
                sender, outgoing.transaction().category(), spentBefore, expensesBefore, balanceBefore);

        return new TransferResult(recipientLogin, normalizedAmount, outgoing.transaction(),
                incoming.transaction(), sender.balance(), notifications);
    }

    private void ensureCategoryType(Wallet wallet, String categoryName, CategoryType expectedType) {
        wallet.categories().find(categoryName).ifPresent(category -> {
            if (category.type() != expectedType) {
                throw new DomainException("Категория «" + category.name() + "» уже существует с типом «"
                        + category.type().label() + "» и мешает переводу. Переименуйте её.");
            }
        });
    }
}
