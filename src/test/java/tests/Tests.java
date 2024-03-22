package tests;

import io.qameta.allure.Step;
import org.junit.Assert;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Tests {
    private WebDriver driver;
    private Actions action;

    @BeforeEach
    public void setUp() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        action = new Actions(driver);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }

    @Test
    @Step("Проверка страницы с чекбоксами")
    public void testCheckboxes() {
        // Открываем страницу с чекбоксами
        driver.get("https://the-internet.herokuapp.com/checkboxes");

        // Находим чекбоксы
        WebElement checkbox1 = driver.findElement(By.xpath("//form[@id='checkboxes']/input[1]"));
        WebElement checkbox2 = driver.findElement(By.xpath("//form[@id='checkboxes']/input[2]"));

        // Изменяем состояние чекбоксов
        if (!checkbox1.isSelected()) {
            checkbox1.click();
        }
        if (checkbox2.isSelected()) {
            checkbox2.click();
        }

        // Выводим в консоль состояние атрибута checked
        System.out.println("Checkbox 1: " + checkbox1.getAttribute("checked"));
        System.out.println("Checkbox 2: " + checkbox2.getAttribute("checked"));
    }
    @Test
    @Step("Проверка страницы Dropdown ")
    public void selectDropdownOptions() {
        driver.get("https://the-internet.herokuapp.com/dropdown");
        WebElement dropdownElement = driver.findElement(By.id("dropdown"));
        Select dropdown = new Select(dropdownElement);

        // Выбираем первую опцию
        dropdown.selectByValue("1");
        System.out.println("Выбранная опция: " + dropdown.getFirstSelectedOption().getText());

        // Выбираем вторую опцию
        dropdown.selectByValue("2");
        System.out.println("Выбранная опция: " + dropdown.getFirstSelectedOption().getText());
    }
    @Test
    @Step("Проверка страницы Disappearing Elements")
    public void testDisappearingElements() {
        int maxAttempts = 10;
        int attempt = 0;
        boolean allElementsFound = false;

        while (attempt < maxAttempts && !allElementsFound) {
            driver.get("https://the-internet.herokuapp.com/disappearing_elements");

            // Найти все элементы меню
            List<WebElement> menuItems = driver.findElements(By.xpath("//div[@class='example']/ul/li/a"));

            if (menuItems.size() == 5) {
                // Если найдено 5 элементов, установить флаг успешного поиска и вывести в консоль их текст
                allElementsFound = true;
                System.out.println("Попытка " + (attempt+1) + ": Найдено все 5 элементов");
                for (WebElement item : menuItems) {
                    System.out.println(item.getText());
                }
            } else {
                // Увеличить счетчик попыток, если не найдено 5 элементов
                System.out.println("Попытка " + (attempt + 1) + ": Найдено " + menuItems.size() + " элементов");
                attempt++;
            }
        }

        // Проверить, были ли найдены все элементы. Если нет, завершить тест с ошибкой.
        Assert.assertTrue("Не удалось найти все 5 элементов за " + maxAttempts + " попыток", allElementsFound);
    }
    @Test
    @Step("Проверка страницы Inputs")
    public void testInputField() {
        // Генерация случайного числа от 1 до 10000
        int randomNumber = new Random().nextInt(10000) + 1;

        // Переход на страницу с полем ввода
        driver.get("https://the-internet.herokuapp.com/inputs");

        // Находим элемент поля ввода по его типу
        WebElement inputField = driver.findElement(By.xpath("//input[@type='number']"));

        // Вводим сгенерированное число в поле ввода
        inputField.sendKeys(String.valueOf(randomNumber));

        // Получаем и выводим в консоль значение из поля ввода
        System.out.println("Введенное значение: " + inputField.getAttribute("value"));
    }
    @Test
    @Step("Проверка страницы Hovers")
    public void testHover() {
        driver.get("https://the-internet.herokuapp.com/hovers");

        // Находим все элементы картинок
        List<WebElement> figures = driver.findElements(By.className("figure"));

        for (WebElement figure : figures) {
            // Используем Actions для наведения курсора на элемент
            action.moveToElement(figure).perform();

            // Находим элемент с информацией, которая появляется при наведении
            WebElement info = figure.findElement(By.className("figcaption"));

            // Выводим текст элемента в консоль
            System.out.println(info.getText());
        }
    }
    @Test
    @Step("Проверка страницы Notification Message")
    public void testNotificationMessage() {
        driver.get("https://the-internet.herokuapp.com/notification_message_rendered");

        boolean actionSuccessful = false;
        while (!actionSuccessful) {
            // Нажимаем на ссылку для загрузки нового сообщения
            driver.findElement(By.linkText("Click here")).click();

            // Проверяем текст всплывающего уведомления
            WebElement message = driver.findElement(By.id("flash"));
            String messageText = message.getText();

            if (messageText.contains("Action successful")) {
                System.out.println("Success message received: " + messageText);
                actionSuccessful = true;
            } else {
                // Если сообщение не содержит "Action successful", ищем и нажимаем на кнопку закрытия уведомления
                System.out.println("Received another message, trying again: " + messageText);
                if (messageText.contains("Action unsuccesful, please try again")) {
                    driver.findElement(By.cssSelector("#flash .close")).click();
                }
            }
        }
    }

    @Test
    @Step("Проверка страницы Add/Remove Elements")
    public void testAddRemoveElements() {
        driver.get("https://the-internet.herokuapp.com/add_remove_elements/");

        // Нажимаем на кнопку "Add Element" 5 раз
        WebElement addButton = driver.findElement(By.xpath("//button[text()='Add Element']"));
        for (int i = 0; i < 5; i++) {
            addButton.click();
            // Выводим в консоль текст появившегося элемента
            System.out.println("Added: " + (i + 1) + " Delete button(s)");
        }

        // Находим все кнопки "Delete" и нажимаем на три из них
        List<WebElement> deleteButtons = driver.findElements(By.className("added-manually"));
        for (int i = 0; i < 3; i++) {
            if (i < deleteButtons.size()) {
                deleteButtons.get(i).click();
                System.out.println("Deleted: " + (i + 1) + " button");
            }
        }

        // После удаления трех кнопок, выводим в консоль оставшееся количество и тексты кнопок "Delete"
        deleteButtons = driver.findElements(By.className("added-manually"));
        System.out.println("Remaining Delete buttons: " + deleteButtons.size());
        for (WebElement button : deleteButtons) {
            System.out.println(button.getText());
        }
    }
    @Test
    @Step("Проверка страницы Status Codes")
    public void testStatusCodes() {
        driver.get("https://the-internet.herokuapp.com/status_codes");

        String[] statusCodes = {"200", "301", "404", "500"};
        for (String statusCode : statusCodes) {
            // Кликаем на каждую ссылку статус-кода
            WebElement link = driver.findElement(By.linkText(statusCode));
            link.click();

            // Выводим текст после перехода на страницу статуса
            WebElement message = driver.findElement(By.cssSelector(".example p"));
            System.out.println("Страница со статусом " + statusCode + ": " + message.getText());

            // Возвращаемся на предыдущую страницу, чтобы кликнуть на следующий статус-код
            driver.navigate().back();
        }
    }

    @AfterEach
    public void tearDown() {
        // Закрываем браузер после выполнения теста
        if (driver != null) {
            driver.quit();
        }
    }
}
