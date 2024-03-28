package tests;

import io.qameta.allure.Step;
import org.junit.Assert;


import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.opentest4j.TestAbortedException;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {
    private WebDriver driver;
    private Actions action;
    private int attempt = 0;
    private static boolean allElementsFound = false;

    private static boolean isPageLoaded = false;


    @BeforeAll
    public void setUp() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        action = new Actions(driver);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "21"})
    void testCheckboxesOrder(String order) {

        openCheckboxesPage();
        clickCheckboxesInOrder(order);

    }


    @Step("Открытие страницы с чекбоксами")
    private void openCheckboxesPage() {
        driver.get("https://the-internet.herokuapp.com/checkboxes");
    }

    @Step("Клик по чекбоксам в порядке: {order}")
    private void clickCheckboxesInOrder(String order) {
        WebElement checkbox1 = driver.findElement(By.xpath("//form[@id='checkboxes']/input[1]"));
        WebElement checkbox2 = driver.findElement(By.xpath("//form[@id='checkboxes']/input[2]"));

        for (char ch : order.toCharArray()) {
            if (ch == '1') {
                clickCheckboxAndVerify(checkbox1, "1");
            } else if (ch == '2') {
                clickCheckboxAndVerify(checkbox2, "2");
            }
        }
    }

    @Step("Клик по чекбоксу {checkboxOrder} и проверка его состояния")
    private void clickCheckboxAndVerify(WebElement checkbox, String checkboxOrder) {
        checkbox.click();
        System.out.println("Checkbox " + checkboxOrder + ": " + (checkbox.isSelected() ? "checked" : "not checked"));
        assertEquals(checkbox.isSelected(), (checkbox.getAttribute("checked") != null));
    }


    @Test
    public void selectDropdownOptions() {

        openDropdownPage();
        selectOption("1");
        verifySelectedOption("Option 1");
        selectOption("2");
        verifySelectedOption("Option 2");

    }


    @Step("Открытие страницы с выпадающим списком")
    private void openDropdownPage() {
        driver.get("https://the-internet.herokuapp.com/dropdown");
    }

    @Step("Выбор опции {option}")
    private void selectOption(String option) {
        WebElement dropdownElement = driver.findElement(By.id("dropdown"));
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByValue(option);
        String selectedOptionText = dropdown.getFirstSelectedOption().getText();
        System.out.println("Выбранная опция: " + selectedOptionText);
    }

    @Step("Проверка выбранной опции: {expectedOptionText}")
    private void verifySelectedOption(String expectedOptionText) {
        WebElement dropdownElement = driver.findElement(By.id("dropdown"));
        Select dropdown = new Select(dropdownElement);
        String actualOptionText = dropdown.getFirstSelectedOption().getText();
        Assertions.assertEquals(expectedOptionText, actualOptionText);
    }


    @RepeatedTest(10)
    public void testDisappearingElements() {
        if (allElementsFound) {
            throw new TestAbortedException("Все элементы уже найдены; пропуск этой итерации.");
        }

        attempt++;
        openPage("https://the-internet.herokuapp.com/disappearing_elements");
        List<WebElement> menuItems = findMenuItems();

        if (menuItems.size() == 5) {
            allElementsFound = true;
            menuItems.forEach(item -> System.out.println(item.getText()));
        }

        if (attempt == 10 && !allElementsFound) {
            Assertions.fail("Не удалось найти все 5 элементов за 10 попыток");
        }
    }

    @Step("Открытие страницы {url}")
    private void openPage(String url) {
        driver.get(url);
    }

    @Step("Поиск пунктов меню на странице")
    private List<WebElement> findMenuItems() {
        return driver.findElements(By.xpath("//div[@class='example']/ul/li/a"));
    }

    @TestFactory
    Collection<DynamicTest> inputFieldTests() {
        // Генерируем 10 случайных чисел для тестов
        Random random = new Random();
        Stream<DynamicTest> positiveTests = random.ints(10, 1, 10001).mapToObj(
                randomNumber -> DynamicTest.dynamicTest("Тест ввода числа " + randomNumber, () -> {
                    testInput(randomNumber);
                })
        );

        // Набор негативных тестовых данных
        Stream<DynamicTest> negativeTests = Stream.of("abc", "!@#", " 123 ", "123a").map(
                input -> DynamicTest.dynamicTest("Негативный тест для: \"" + input + "\"", () -> {
                    testInvalidInput(input);
                })
        );


        return Stream.concat(positiveTests, negativeTests).collect(Collectors.toList());
    }

    @Step("Проверка ввода числа {number} в поле ввода")
    private void testInput(int number) {
        driver.get("https://the-internet.herokuapp.com/inputs");
        WebElement inputField = driver.findElement(By.xpath("//input[@type='number']"));
        inputField.sendKeys(String.valueOf(number));
        Assertions.assertEquals(String.valueOf(number), inputField.getAttribute("value"), "Введенное значение не соответствует ожидаемому");
    }

    @Step("Проверка ввода невалидных данных \"{input}\" в поле ввода")
    private void testInvalidInput(String input) {
        driver.get("https://the-internet.herokuapp.com/inputs");
        WebElement inputField = driver.findElement(By.xpath("//input[@type='number']"));
        inputField.sendKeys(input);
        // Для невалидных данных поле должно быть пустым или содержать только часть валидного ввода (например, числа из строки с числами и буквами)
        boolean isValid = inputField.getAttribute("value").matches("^\\d*$");
        Assertions.assertTrue(isValid, "Поле ввода содержит невалидные данные");
    }

    static Stream<Object[]> hoverData() {
        return Stream.of(
                new Object[]{1, "name: user1"},
                new Object[]{2, "name: user2"},
                new Object[]{3, "name: user3"}
        );
    }

    @ParameterizedTest
    @MethodSource("hoverData")
    public void testHoverOnEachFigure(int figureIndex, String expectedText) {
        openPage("https://the-internet.herokuapp.com/hovers");
        hoverAndCheckText(figureIndex, expectedText);
    }


    @Step("Наведение на элемент {figureIndex} и проверка текста")
    private void hoverAndCheckText(int figureIndex, String expectedText) {
        List<WebElement> figures = driver.findElements(By.className("figure"));
        WebElement figure = figures.get(figureIndex - 1);
        action.moveToElement(figure).perform();

        WebElement info = figure.findElement(By.className("figcaption"));
        String actualText = info.getText();
        System.out.println("Actual Text: " + actualText);
        assertTrue(actualText.contains(expectedText), "Text does not match!");
    }

    @RepeatedTest(5)
    public void testNotificationMessage(RepetitionInfo repetitionInfo) {
        if (!isPageLoaded) {
            driver.get("https://the-internet.herokuapp.com/notification_message_rendered");
            isPageLoaded = true;
        }

        driver.findElement(By.linkText("Click here")).click();
        WebElement message = new WebDriverWait(driver, 5)
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("flash")));
        String messageText = message.getText().trim();

        // Проверка, содержит ли сообщение "Action successful"
        boolean isActionSuccessful = messageText.contains("Action successful");

        // Выводим полученное сообщение в консоль
        System.out.println("Message received: " + messageText);


        Assertions.assertTrue(isActionSuccessful, "The notification message was not successful. Message received: " + messageText);


    }


    @TestFactory
    Stream<DynamicTest> testAddRemoveElementsDynamically() {
        List<int[]> parameters = List.of(
                new int[]{2, 1},
                new int[]{5, 2},
                new int[]{1, 3}
        );

        return parameters.stream().map(params -> DynamicTest.dynamicTest(
                "Test with add " + params[0] + " elements and remove " + params[1],
                () -> {
                    openTestPage();
                    addElements(params[0]);
                    verifyNumberOfDeleteButtonsAfterAdding(params[0]);
                    removeElements(params[1]);
                    verifyNumberOfDeleteButtonsAfterRemoval(params[0] - params[1]);
                }
        ));
    }

    @Step("Открытие тестовой страницы")
    private void openTestPage() {
        driver.get("https://the-internet.herokuapp.com/add_remove_elements/");
    }

    @Step("Добавление {0} элементов")
    private void addElements(int numberOfElementsToAdd) {
        WebElement addButton = driver.findElement(By.xpath("//button[text()='Add Element']"));
        for (int i = 0; i < numberOfElementsToAdd; i++) {
            addButton.click();
        }
    }

    @Step("Проверка количества кнопок 'Удалить' после добавления. Ожидается: {0}")
    private void verifyNumberOfDeleteButtonsAfterAdding(int expectedNumber) {
        List<WebElement> deleteButtons = driver.findElements(By.className("added-manually"));
        assertEquals(expectedNumber, deleteButtons.size(), "Количество кнопок 'Удалить' не соответствует ожидаемому после добавления.");
    }

    @Step("Удаление {0} элементов")
    private void removeElements(int numberOfElementsToRemove) {
        List<WebElement> deleteButtons = driver.findElements(By.className("added-manually"));
        for (int i = 0; i < numberOfElementsToRemove; i++) {
            if (!deleteButtons.isEmpty()) {
                deleteButtons.get(i).click();
                deleteButtons = driver.findElements(By.className("added-manually")); // Обновляем список элементов
            }
        }
    }

    @Step("Проверка количества кнопок 'Удалить' после удаления. Ожидается: {0}")
    private void verifyNumberOfDeleteButtonsAfterRemoval(int expectedNumberAfterRemoval) {
        List<WebElement> deleteButtons = driver.findElements(By.className("added-manually"));
        assertEquals(Math.max(expectedNumberAfterRemoval, 0), deleteButtons.size(), "Количество кнопок 'Удалить' не соответствует ожидаемому после удаления.");
    }


    @Test
    public void testEachStatusCodeLink() {
        driver.get("https://the-internet.herokuapp.com/status_codes");

        String[] statusCodes = {"200", "301", "404", "500"};
        for (String statusCode : statusCodes) {
            // Кликаем на каждую ссылку статус-кода
            WebElement link = driver.findElement(By.linkText(statusCode));
            link.click();

            // Выводим текст после перехода на страницу статуса
            WebElement message = driver.findElement(By.cssSelector(".example p"));
            System.out.println("Страница со статусом " + statusCode + ": " + message.getText());

            // Проверяем, что URL содержит нужный код статуса как часть пути
            assertTrue(driver.getCurrentUrl().contains("/status_codes/" + statusCode), "Переход был осуществлен на страницу с некорректным статусом.");

            // Возвращаемся на предыдущую страницу, чтобы кликнуть на следующий статус-код
            driver.navigate().back();
        }
    }

    @AfterAll
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}
