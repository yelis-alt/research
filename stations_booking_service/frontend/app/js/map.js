//инициализация карты
document.body.style.zoom = "125%";

ymaps.ready(init, );

function coordize(desc, n, p) {
    return desc["properties"]["waypoints"][n]["coordinates"][p].toString();
}

let start;
let finish;
function init() {
    myMap = new ymaps.Map('map', {
        center: [lati, long],
        zoom: 10,
        controls: []
    })
        routePanelControl = new ymaps.control.RoutePanel({
            options: {
                maxWidth: '351px',
                showHeader: true,
                title: ' '
            }
        })
        zoomControl = new ymaps.control.ZoomControl({
            options: {
                size: 'small',
                float: 'none',
                position: {
                    bottom: 145,
                    right: 10
                }
            }
        });
    routePanelControl.routePanel.options.set({
        types: {auto: true}
    });
    myMap.controls.add(routePanelControl).add(zoomControl);
    routePanelControl.routePanel.getRouteAsync().then(function (route) {
        route.model.setParams({results: 1}, true);
        route.model.events.add('requestsuccess', function () {
            let activeRoute = route.getActiveRoute();
            if (activeRoute) {
                let length = route.getActiveRoute().properties.get("distance"),
                    balloonContentLayout = ymaps.templateLayoutFactory.createClass(
                        '<span>Расстояние: ' + length.text + '\xa0\xa0\xa0\xa0</span>');
                route.options.set('routeBalloonContentLayout', balloonContentLayout);
                activeRoute.balloon.open();
                let desc = route.model.getJson()
                start = coordize(desc, 0, 1) + ',' + coordize(desc, 0, 0);
                finish = coordize(desc, 1, 1) + ',' + coordize(desc, 1, 0);
            }
        });
    });
}

//функции для работы с метками на карте
let geoObj = [];
function build(ind, caption, img){
    myPlacemarkWithContent = new ymaps.Placemark([ind.latitude, ind.longitude], {
        balloonContent: caption
    }, {
        iconLayout: 'default#imageWithContent',
        iconImageHref: img,
        iconImageSize: [25, 25],
        iconImageOffset: [-10, -10],
        iconContentOffset: [-8, 10],
        iconContentLayout: MyIconContentLayout
    });
    myMap.geoObjects.add(myPlacemarkWithContent);
    geoObj.push(myPlacemarkWithContent);
}

function eletypize(){
    let acdc = [];
    let electri = $('#plugType input[type=checkbox]:checked');
    for (let i = 0; i < electri.length; i++) {
        acdc.push(electri.eq(i).val());
    }
    return acdc;
}

function tokize(str) {
    if (str === 'AC') {
        return ' (Переменный ток)';
    } else {
        return ' (Постоянный ток)';
    }
}

function eraseMap(){
    $('.ymaps-2-1-79-map').remove();
    init();
}

//нанесение меток на карту
let imgPos = '/frontend/images/c.png';
let imgNeg = '/frontend/images/d.png';
let plug = 'TYPE_2';
let plugPath = '/frontend/images/m_type2.png';
let plugType = ["AC", "DC"];
let fromPower = 10;
let toPower = 50;
let fromPrice = 0;
let toPrice = 60;
let stationsList;
function getStations() {
    if (geoObj.length !== 0) {
        eraseMap();
    }

    $.ajax({
        type: 'POST',
        url: 'http://localhost:8080/routing/getFilteredStationsList',
        data: JSON.stringify({
            plug: plug,
            plugType: plugType,
            fromPower: fromPower,
            toPower: toPower,
            fromPrice: fromPrice,
            toPrice: toPrice
        }),
        dataType: 'json',
        contentType: "application/json",
        success: function (data) {
            stationsList = data
            ymaps.ready(function () {
                MyIconContentLayout = ymaps.templateLayoutFactory.createClass(
                    '<div>$[properties.iconContent]</div>'
                );
                $.each(stationsList, function (index) {
                    let caption_pos = '№' + String(stationsList[index].id) + '<br/>' +
                        '---------------------------' + '<br/>' +
                        'Адрес: ' + String(stationsList[index].address) + '<br/>' +
                        'Компания: ' + String(stationsList[index].company) + '<br/>' +
                        'Тип тока: ' + String(stationsList[index].plug_type).toUpperCase() + tokize(stationsList[index].plug_type) + '<br/>' +
                        'Мощность: ' + String(stationsList[index].power).toUpperCase() + " кВт" + '<br/>' +
                        '<img src=' + '"' + plugPath + '"' + '</img>' + '<br/>' +
                        '---------------------------' + '<br/>' +
                        String(stationsList[index].price) + ' руб. за 1 кВт';
                    let caption_neg = '№' + String(stationsList[index].id) + '<br/>' +
                        '---------------------------' + '<br/>' + 'ЭЗС временно недоступна';
                    if (stationsList[index].status === 1) {
                        build(stationsList[index], caption_pos, imgPos);
                        build(stationsList[index], caption_pos, imgPos);
                    } else {
                        build(stationsList[index], caption_neg, imgNeg);
                        build(stationsList[index], caption_neg, imgNeg);
                    }
                })
            });
        }
    })
}

window.addEventListener('DOMContentLoaded', function() {
    getStations();
});

//изменение характеристик ЭЗС
$('[name="charger_selector"]').click(function() {
    plug = $('input[name="charger_selector"]:checked').val();
    plugPath = '/frontend/images/'+'m_'+plug+'.png';
    getStations();
});

$('#ac, #dc, #fromInput_kvt, #toInput_kvt, ' +
  '#fromInput_price, #toInput_price, ' +
  '#fromSlider_kvt, #toSlider_kvt, ' +
  '#fromSlider_price, #toSlider_price').on('change', function() {
    plugType = '('+eletypize()+')';
    plugType = plugType.replace('AC', '"AC"')
                 .replace('DC','"DC"');
    fromPower = $('#fromInput_kvt').val();
    toPower = $('#toInput_kvt').val();
    fromPrice = $('#fromInput_price').val();
    toPrice = $('#toInput_price').val();
    getStations();
});


function getRouteId(idsi) {
    let routeStat = [starti]
    $.each(idsi, function (index, value) {
        let idf = stationsList.findIndex(obj => obj.id === value);
        let next_point = {type: 'viaPoint',
                          point: [stationsList[idf].latitude, stationsList[idf].longitude]};

        routeStat.push(next_point);
    });
    routeStat.push(finishi)
    return routeStat
}

//построение оптимального маршрута
let starti;
let finishi;
function initializeBooking(idsi) {
    starti = [start.split(',')[0], start.split(',')[1]]
    finishi = [finish.split(',')[0], finish.split(',')[1]]
    let myMap = new ymaps.Map("map", {
        center: starti,
        zoom: 13
    });
    ymaps.route(getRouteId(idsi), {
        mapStateAutoApply: true
    }).then(function (route) {
        myMap.geoObjects.add(route);
    });
    for (let i = 0; i < 2; i++){
        myMap.geoObjects.add(new ymaps.Placemark(starti, {
            balloonContent: 'Cтарт'
        }, {
            iconLayout: 'default#imageWithContent',
            iconImageHref: '/frontend/images/a.png',
            iconImageSize: [25, 25],
            iconImageOffset: [-10, -10],
            iconContentOffset: [-8, 10],
            iconContentLayout: MyIconContentLayout
        }));
        $.each(ids, function (index, value){
            let idf = stationsList.findIndex(obj => obj.id === value)
            MyIconContentLayout = ymaps.templateLayoutFactory.createClass(
                '<div>$[properties.iconContent]</div>'
            );
            let capt = '№ ' + String(stationsList[idf].id) + '<br/>' +
                '---------------------------' + '<br/>' +
                'Адрес: ' + String(stationsList[idf].address) + '<br/>' +
                'Компания: ' + String(stationsList[idf].company) + '<br/>' +
                'Тип тока: ' + String(stationsList[idf].plugType).toUpperCase() + tokize(stationsList[idf].plugType) + '<br/>' +
                'Мощность: '+ String(stationsList[idf].power).toUpperCase() + " кВт" + '<br/>' +
                '<img src=' + '"' + plugPath + '"' + '</img>' + '<br/>' +
                '---------------------------' + '<br/>' +
                String(stationsList[idf].price) + ' руб. за 1 кВт';
            myPlacemarkWithContent = new ymaps.Placemark([stationsList[idf].latitude, stationsList[idf].longitude], {
                balloonContent: capt
            }, {
                iconLayout: 'default#imageWithContent',
                iconImageHref: '/frontend/images/c.png',
                iconImageSize: [25, 25],
                iconImageOffset: [-10, -10],
                iconContentOffset: [-8, 10],
                iconContentLayout: MyIconContentLayout
            });
            myMap.geoObjects.add(myPlacemarkWithContent);
        });
        myMap.geoObjects.add(new ymaps.Placemark(finishi, {
            balloonContent: 'Финиш'
        }, {
            iconLayout: 'default#imageWithContent',
            iconImageHref: '/frontend/images/b.png',
            iconImageSize: [25, 25],
            iconImageOffset: [-10, -10],
            iconContentOffset: [-8, 10],
            iconContentLayout: MyIconContentLayout
        }));
    }
}

function booking_display(){
    $('#loading').hide();
    $('#map').show();
    $('.ymaps-2-1-79-map').remove();
    let idsi = ids;
    initializeBooking(idsi);
    $('#map').css({
        'margin-top': '5.5px',
        'height': '523px'
    });
    $('.ymaps-2-1-79-islets_icon-with-caption').css({
        'display': 'none'
    });
    $('#booking').show();
    $('.ymaps-2-1-79-map').css({
        'height': '518px'
    });
}
function displayMap(){
    setTimeout(function() {
        $('.ymaps-2-1-79-islets_icon-with-caption').remove();
        $('.ymaps-2-1-79-image').remove();
        $('#map').attr("id","mapi");
        $('.ymaps-2-1-79-map.' +
            'ymaps-2-1-79-i-ua_js_yes.' +
            'ymaps-2-1-79-map-bg.' +
            'ymaps-2-1-79-islets_map-lang-ru').attr("class","mapi__land-inner");
        if ($(window).width() < 1024){
            $('.ymaps-2-1-79-map').css({
                'height': '613px'
            });
        }
        setTimeout(function(){
            if (ids.length === 0){
                alert('Для прохождения маршрута подзарядок не требуется')
            }
        }, 1000)

    }, 1000);
}

function solution(){
    let acc = $('.parameter__temp-acc').val();
    let maxacc = $('.parameter__temp-maxacc').val();
    let spend = $('.parameter__temp-spend').val();
    let grad = $('.weather__temp-number').val();
    let data = {jsc: stationsList,
                acc: acc,
                maxacc: maxacc,
                spend: spend,
                grad: grad,
                start: start,
                finish: finish};
    $('.ymaps-2-1-79-route-panel__clear').click();
    $('.tabs').hide();
    $('.ymaps-2-1-79-controls__control_toolbar').hide();
    $('.routing__legendbox').hide();
    $('.ymaps-2-1-79-zoom').hide();
    $('#ok').hide();
    $('#map').hide();
    $('#loading').show();
    $('.textbox').css({
        'margin-top': '3.25px'
    });
    $.ajax({
        type:'POST',
        url: 'http://localhost:5000/',
        dataType : 'json',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(data){
            login = Object.keys(data)[0];
            ids = data[login];
            if (ids === 'impossible'){
                alert('К сожалению, согласно заданными Вами параметрами ' +
                    'построение маршрута невозможно.\n' +
                    'Вы можете поменять их и попробовать снова.');
                window.location.reload();
            }else{
                ids = ids.slice(1, -1);
                $('.booking__station input[type=number]').val(ids[0]);
                booking_panel();
                booking_display();
                extender();
                displayMap();
            }
        }
    });
}
//подтверждение маршрута
let ids;
let login;
$('#ok').click(function(){
    preventSend();
    if (($('.ymaps-2-1-79-route-panel-input__input').eq(0).val() != '') &
        ($('.ymaps-2-1-79-route-panel-input__input').eq(1).val() != '')){
        solution()
        }else{
        if (($('.ymaps-2-1-79-route-panel-input__input').eq(0).val() == '') &
            ($('.ymaps-2-1-79-route-panel-input__input').eq(1).val() == '')) {
            alert('Пожалуйста, выберите пункты отправления и прибытия');
            preventSend();
        }else{
            if ($('.ymaps-2-1-79-route-panel-input__input').eq(0).val() == '') {
                alert('Пожалуйста, выберите пункт отправления');
                preventSend();
            }else{
                alert('Пожалуйста, выберите пункт прибытия');
                preventSend();
            }
        }
    }
});

$('#window_repeat').click(function(){

})
