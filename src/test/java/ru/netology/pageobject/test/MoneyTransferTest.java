package ru.netology.pageobject.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.pageobject.data.DataHelper;
import ru.netology.pageobject.page.DashboardPage;
import ru.netology.pageobject.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.pageobject.data.DataHelper.*;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup() {
        var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        secondCardBalance = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
    }

    @Test
    void shouldTransferMoneyFrom1to2() {
        var amount = generateValidAmount(firstCardBalance);
        var expected1CardBalance = firstCardBalance - amount;
        var expected2CardBalance = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        var actual1CardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actual2CardBalance = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
        assertAll(() -> assertEquals(expected1CardBalance, actual1CardBalance),
                () -> assertEquals(expected2CardBalance, actual2CardBalance));
    }

    @Test
    void shouldHaveErrorIfTransferMoreThanBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания");
        var actual1CardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actual2CardBalance = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
        assertAll(() -> assertEquals(firstCardBalance, actual1CardBalance),
                () -> assertEquals(secondCardBalance, actual2CardBalance));

    }
}
