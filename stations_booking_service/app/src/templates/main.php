<!DOCTYPE html>
<html lang = "ru">
<head>
    <meta charset="UTF-8">
    <title>ChargeBook</title>
    <meta name = "viewpoint" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="frontend/css/style.min.css">
    <link rel="icon" type="image/x-icon" href="frontend/images/favicon.ico">
</head>
<body>
<div class="tabs">
    <button class="tabs-link" onclick="openPage('parameters', this)" id="defaultOpen">Сведения</button>
    <button class="tabs-link" onclick="openPage('routing', this)" id="secondOpen">Маршрутизация и бронирование</button>
</div>
<div class="head_container">
    <header class="header">
        <a class="header-logo" href="https://xn----7sb7akeedqd.xn--p1ai/platform/portal/tehprisEE_portal">
            <img src="frontend/images/logo.svg" alt="">
        </a>
        <div class="city">
            <select class="city-select" id="select-city" aria-label="Default select example">
                <option value="Москва">Москва</option>
                <option value="Санкт-Петербург">Санкт-Петербург</option>
                <option value="Абакан">Абакан</option>
                <option value="Александров">Александров</option>
                <option value="Анадырь">Анадырь</option>
                <option value="Анапа">Анапа</option>
                <option value="Армавир">Армавир</option>
                <option value="Архангельск">Архангельск</option>
                <option value="Астрахань">Астрахань</option>
                <option value="Ачинск">Ачинск</option>
                <option value="Балаково">Балаково</option>
                <option value="Барнаул">Барнаул</option>
                <option value="Белгород">Белгород</option>
                <option value="Бердск">Бердск</option>
                <option value="Бийск">Бийск</option>
                <option value="Биробиджан">Биробиджан</option>
                <option value="Благовещенск">Благовещенск</option>
                <option value="Братск">Братск</option>
                <option value="Брянск">Брянск</option>
                <option value="Великие Луки">Великие Луки</option>
                <option value="Великий Новгород">Великий Новгород</option>
                <option value="Владивосток">Владивосток</option>
                <option value="Владикавказ">Владикавказ</option>
                <option value="Владимир">Владимир</option>
                <option value="Волгоград">Волгоград</option>
                <option value="Волгодонск">Волгодонск</option>
                <option value="Вологда">Вологда</option>
                <option value="Воронеж">Воронеж</option>
                <option value="Геленджик">Геленджик</option>
                <option value="Горно-Алтайск">Горно-Алтайск</option>
                <option value="Грозный">Грозный</option>
                <option value="Гусь-Хрустальный">Гусь-Хрустальный</option>
                <option value="Дзержинск">Дзержинск</option>
                <option value="Долгопрудный">Долгопрудный</option>
                <option value="Дубна">Дубна</option>
                <option value="Ейск">Ейск</option>
                <option value="Екатеринбург">Екатеринбург</option>
                <option value="Ессентуки">Ессентуки</option>
                <option value="Железногорск">Железногорск</option>
                <option value="Жигулевск">Жигулевск</option>
                <option value="Зеленоград">Зеленоград</option>
                <option value="Иваново">Иваново</option>
                <option value="Ижевск">Ижевск</option>
                <option value="Иркутск">Иркутск</option>
                <option value="Йошкар-Ола">Йошкар-Ола</option>
                <option value="Казань">Казань</option>
                <option value="Калининград">Калининград</option>
                <option value="Калуга">Калуга</option>
                <option value="Каменск-Уральский">Каменск-Уральский</option>
                <option value="Каменск-Шахтинский">Каменск-Шахтинский</option>
                <option value="Кемерово">Кемерово</option>
                <option value="Киров">Киров</option>
                <option value="Кисловодск">Кисловодск</option>
                <option value="Комсомольск-на-Амуре">Комсомольск-на-Амуре</option>
                <option value="Кострома">Кострома</option>
                <option value="Краснодар">Краснодар</option>
                <option value="Красноярск">Красноярск</option>
                <option value="Курган">Курган</option>
                <option value="Курск">Курск</option>
                <option value="Кызыл">Кызыл</option>
                <option value="Липецк">Липецк</option>
                <option value="Магадан">Магадан</option>
                <option value="Магнитогорск">Магнитогорск</option>
                <option value="Майкоп">Майкоп</option>
                <option value="Махачкала">Махачкала</option>
                <option value="Междуреченск">Междуреченск</option>
                <option value="Минеральные Воды">Минеральные Воды</option>
                <option value="Мурманск">Мурманск</option>
                <option value="Муром">Муром</option>
                <option value="Набережные Челны">Набережные Челны</option>
                <option value="Нальчик">Нальчик</option>
                <option value="Находка">Находка</option>
                <option value="Нефтекамск">Нефтекамск</option>
                <option value="Нижневартовск">Нижневартовск</option>
                <option value="Нижнекамск">Нижнекамск</option>
                <option value="Нижний Новгород">Нижний Новгород</option>
                <option value="Нижний Тагил">Нижний Тагил</option>
                <option value="Новокузнецк">Новокузнецк</option>
                <option value="Новороссийск">Новороссийск</option>
                <option value="Новосибирск">Новосибирск</option>
                <option value="Новоуральск">Новоуральск</option>
                <option value="Новочеркасск">Новочеркасск</option>
                <option value="Норильск">Норильск</option>
                <option value="Обнинск">Обнинск</option>
                <option value="Омск">Омск</option>
                <option value="Орёл">Орёл</option>
                <option value="Оренбург">Оренбург</option>
                <option value="Пенза">Пенза</option>
                <option value="Первоуральск">Первоуральск</option>
                <option value="Пермь">Пермь</option>
                <option value="Петрозаводск">Петрозаводск</option>
                <option value="Петропавловск-Камчатский">Петропавловск-Камчатский</option>
                <option value="Прокопьевск">Прокопьевск</option>
                <option value="Псков">Псков</option>
                <option value="Пущино">Пущино</option>
                <option value="Пятигорск">Пятигорск</option>
                <option value="Ростов-на-Дону">Ростов-на-Дону</option>
                <option value="Рубцовск">Рубцовск</option>
                <option value="Рязань">Рязань</option>
                <option value="Салават">Салават</option>
                <option value="Салехард">Салехард</option>
                <option value="Самара">Самара</option>
                <option value="Саранск">Саранск</option>
                <option value="Саратов">Саратов</option>
                <option value="Саров">Саров</option>
                <option value="Северодвинск">Северодвинск</option>
                <option value="Смоленск">Смоленск</option>
                <option value="Снежинск">Снежинск</option>
                <option value="Сочи">Сочи</option>
                <option value="Ставрополь">Ставрополь</option>
                <option value="Стерлитамак">Стерлитамак</option>
                <option value="Сургут">Сургут</option>
                <option value="Сызрань">Сызрань</option>
                <option value="Сыктывкар">Сыктывкар</option>
                <option value="Таганрог">Таганрог</option>
                <option value="Тамбов">Тамбов</option>
                <option value="Тверь">Тверь</option>
                <option value="Тобольск">Тобольск</option>
                <option value="Тольятти">Тольятти</option>
                <option value="Туапсе">Туапсе</option>
                <option value="Тула">Тула</option>
                <option value="Тюмень">Тюмень</option>
                <option value="Улан-Удэ">Улан-Удэ</option>
                <option value="Ульяновск">Ульяновск</option>
                <option value="Уссурийск">Уссурийск</option>
                <option value="Уфа">Уфа</option>
                <option value="Хабаровск">Хабаровск</option>
                <option value="Ханты-Мансийск">Ханты-Мансийск</option>
                <option value="Чебоксары">Чебоксары</option>
                <option value="Челябинск">Челябинск</option>
                <option value="Черкесск">Черкесск</option>
                <option value="Чита">Чита</option>
                <option value="Шахты">Шахты</option>
                <option value="Элиста">Элиста</option>
                <option value="Южно-Сахалинск">Южно-Сахалинск</option>
                <option value="Якутск">Якутск</option>
                <option value="Ярославль">Ярославль</option>
            </select>
        </div>
    </header>
</div>
<div class="textbox">
    <h1 class="textbox-title">
        Бронирование электрозарядной станции
    </h1>
</div>
<div id="parameters" class="tabcontent">
    <div class="charger">
        <p class = "charger-title">
            Характеристики электрозарядной станции
        </p>
        <p class = "charger__type-title">
            Тип штекера:
        </p>
        <div class="charger__type__chooser">
            <div id="charger__type__chooser">
                <input type="radio" id="charger1" name="charger_selector" value="type2" checked/>
                <label class = "charger__type__chooser-radio1" for="charger1"></label>
                <input type="radio" id="charger2" name="charger_selector" value="chademo"/>
                <label class = "charger__type__chooser-radio2" for="charger2"></label>
                <input type="radio" id="charger3" name="charger_selector" value="ccscombo2"/>
                <label class = "charger__type__chooser-radio3" for="charger3"></label>
                <input type="radio" id="charger4" name="charger_selector" value="saej1772"/>
                <label class = "charger__type__chooser-radio4" for="charger4"></label>
                <input type="radio" id="charger5" name="charger_selector" value="ccscombo1"/>
                <label class = "charger__type__chooser-radio5" for="charger5"></label>
                <input type="radio" id="charger6" name="charger_selector" value="tesla"/>
                <label class = "charger__type__chooser-radio6" for="charger6"></label>
                <input type="radio" id="charger7" name="charger_selector" value="gbtac"/>
                <label class = "charger__type__chooser-radio7" for="charger7"></label>
                <input type="radio" id="charger8" name="charger_selector" value="gbtdc"/>
                <label class = "charger__type__chooser-radio8" for="charger8"></label>
            </div>
        </div>
        <p class = "charger__torrent-title">
            Тип электрического тока:
        </p>
        <div class="charger__torrent">
            <fieldset id="ac_dc">
                <div>
                    <input type="checkbox" id="ac" name="torrent" value = "ac" checked>
                    <label for="ac">AC (переменный ток)</label>
                </div>
                <div>
                    <input type="checkbox" id="dc" name="torrent" value = "dc" checked>
                    <label for="dc">DC (постоянный ток)</label>
                </div>
            </fieldset>
        </div>
        <p class="charger__kvt-title">
            Диапозон мощности (кВт):
        </p>
        <div class="charger__kvt">
            <div class="charger__kvt__container">
                <div class="charger__kvt__control-form">
                    <div class="charger__kvt__control-form__container">
                        <div class="form__control__container-time"></div>
                        <input type="number" id="fromInput_kvt" value="10" min="0" max="100"/>
                    </div>
                    <div class="charger__kvt__control-form__container">
                        <div class="form__control__container-time"></div>
                        <input type="number" id="toInput_kvt" value="50" min="0" max="100"/>
                    </div>
                </div>
                <div class="charger__kvt__control-sliders">
                    <input id="fromSlider_kvt" type="range" value="10" min="0" max="100"/>
                    <input id="toSlider_kvt" type="range" value="50" min="0" max="100"/>
                </div>
            </div>
        </div>
        <p class="charger__price-title">
            Диапозон цены за потребление 1 кВт (руб.):
        </p>
        <div class="charger__price">
            <div class="charger__price__container">
                <div class="charger__price__container__control">
                    <div class="charger__price__control-form__container">
                        <div class="form__control__container-time"></div>
                        <input type="number" id="fromInput_price" value="0" min="0" max="100"/>
                    </div>
                    <div class="form_control_container1">
                        <div class="form__control__container-time"></div>
                        <input type="number" id="toInput_price" value="60" min="0" max="100"/>
                    </div>
                </div>
                <div class="charger__price__control-sliders">
                    <input id="fromSlider_price" type="range" value="0" min="0" max="100"/>
                    <input id="toSlider_price" type="range" value="60" min="0" max="100"/>
                </div>
            </div>
        </div>
    </div>
    <div class="model">
        <div class="model-title">
            Модель электрокара:
        </div>
        <select class="model-select" id="select-model" aria-label="Default select example" >
            <option value="volkswagen_id_4" selected="selected">Volkswagen ID.4</option>
            <option value="evolute-i-pro" selected="selected">Evolute i-PRO</option>
        </select>
    </div>
    <div class="parameter">
        <div class = "parameter-title">
            Технические параметры электрокара
        </div>
        <div class="parameter__temp">
            <div class = "parameter__temp-title">
                Текущий уровень заряда аккумулятора:
            </div>
            <input class = "parameter__temp-acc" type="number" min = "0" max ="100" value="100">
            <div class = "parameter__temp-accg">
                %
            </div>
            <div class="line1"></div>
            <div class = "parameter__temp-title">
                Заявленная ёмкость аккумулятора:
            </div>
            <input class = "parameter__temp-maxacc" type="number" min = "0" max ="999" value="0" readonly>
            <div class = "parameter__temp-maxaccg">
                кВт-ч
            </div>
            <div class="line2"></div>
            <div class = "parameter__temp-title">
                Заявленный расход электроэнергии на 100 км:
            </div>
            <input class = "parameter__temp-spend" type="number" min = "0" max ="999" value="0" readonly>
            <div class = "parameter__temp-spendg">
                кВт-ч/км
            </div>
        </div>
    </div>
    <div class="weather">
        <div class = "weather-title">
            Погодные условия
        </div>
        <div class="weather__temp">
            <div class = "weather__temp-date">
                Предполагаемая дата поездки:
            </div>
            <input class = "weather__temp-date-picker" id = "trip_date" type="date" value="2021-01-24"/>
            <div class="line3"></div>
            <div class = "weather__temp-title">
                Усреднённая температура в течение дня:
            </div>
            <input class = "weather__temp-number" type="number" readonly/>
            <div class = "weather__temp-grad">
                °C
            </div>
        </div>
    </div>
</div>
<div id="routing" class="tabcontent">
    <div id="map">
    </div>
    <div id="booking" class="booking">
        <div class="booking__station">
            <button class="booking__station-back">
                ᐊ
            </button>
            <span class="booking__station-desc">
                     &nbsp;Зарядный слот №
                </span>
            <input type="number" class="booking__station-id" readonly/>
            <button class="booking__station-forward">
                &nbsp;ᐅ
            </button>
        </div>
        <div class="booking__period">
        </div>
        <div class="booking__buttons">
            <form id="register">
                <button id="window_send">
                    &nbsp;Бронь
                </button>
                <button id="window_repeat">
                    &nbsp;Другой маршрут
                </button>
            </form>
        </div>
    </div>
    <div class="routing__legendbox">
        <img src="/frontend/images/a.png" alt="">
        <div>
                <span>
                - начальная точка маршрута
                </span>
        </div><br>
        <img src="/frontend/images/b.png" alt="">
        <div>
                <span>
                    - конечная точка маршрута
                </span>
        </div><br>
        <img src="/frontend/images/c.png" alt="">
        <div>
                <span>
                    - доступная ЭЗС
                </span>
        </div><br>
        <img src="/frontend/images/d.png" alt="">
        <div>
                <span>
                    - недоступная ЭЗС
                </span>
        </div>
    </div>
    <div class="routing__control">
        <form id="submit">
            <button type="submit" id="ok" value = "OK">
                ОК
            </button>
        </form>
    </div>
</div>
<img src="/frontend/images/loading.gif" alt="" id="loading">
<script charset="utf-8" src="https://api-maps.yandex.ru/1.1/index.xml" type="text/javascript"></script>
<script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU&amp;apikey=63dc68ac-a69f-4528-b18f-4cca082ceefc" type="text/javascript"></script>
<script src="frontend/js/main.min.js" type="text/javascript"></script>
</body>
</html>