//инициализация карты
ymaps.ready(init);

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
let geo_obj = [];
function build(ind, caption, img){
    myPlacemarkWithContent = new ymaps.Placemark([ind.lat, ind.lon], {
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
    geo_obj.push(myPlacemarkWithContent);
}

function eletypize(){
    let acdc = [];
    let electri = $('#ac_dc input[type=checkbox]:checked');
    for (let i = 0; i < electri.length; i++) {
        acdc.push(electri.eq(i).val());
    }
    return acdc;
}

function tokize(str) {
    if (str == 'ac') {
        return ' (Переменный ток)';
    } else {
        return ' (Постоянный ток)';
    }
}

function map_erase(){
    $('.ymaps-2-1-79-map').remove();
    init();
}

//нанесение меток на карту
let img_pos = '/frontend/images/c.png';
let img_neg = '/frontend/images/d.png';
let plug = 'type2';
let plug_path = '/frontend/images/m_type2.png';
let ac_dc = '("ac","dc")';
let from_power = 10;
let to_power = 50;
let from_price = 0;
let to_price = 60;
let jsc;

function json_take() {
    if (geo_obj.length != 0){
        map_erase();
    }
    $.ajax({
        type:'POST',
        url: 'stations',
        data: JSON.stringify({plug: plug,
                                   ac_dc: ac_dc,
                                   from_power: from_power,
                                   to_power: to_power,
                                   from_price: from_price,
                                   to_price: to_price}),
        dataType : 'json',
        contentType: "application/json",
        success: function(data){
            jsc = data;
            ymaps.ready(function () {
                MyIconContentLayout = ymaps.templateLayoutFactory.createClass(
                    '<div>$[properties.iconContent]</div>'
                );
                $.each(jsc, function(index){
                    let caption_pos = '№' + String(jsc[index].id) + '<br/>' +
                                      '---------------------------' + '<br/>' +
                        'Адрес: ' + String(jsc[index].address) + '<br/>' +
                        'Компания: ' + String(jsc[index].company) + '<br/>' +
                        'Тип тока: ' + String(jsc[index].plug_type).toUpperCase() + tokize(jsc[index].plug_type) + '<br/>' +
                        'Мощность: '+ String(jsc[index].power).toUpperCase() + " кВт" + '<br/>' +
                        '<img src=' + '"' + plug_path + '"' + '</img>' + '<br/>' +
                        '---------------------------' + '<br/>' +
                        String(jsc[index].price) + ' руб. за 1 кВт';
                    let caption_neg = '№' + String(jsc[index].id) + '<br/>' +
                                      '---------------------------' + '<br/>' + 'ЭЗС временно недоступна';
                    if (jsc[index].status == 1) {
                        build(jsc[index], caption_pos, img_pos);
                        build(jsc[index], caption_pos, img_pos);
                    } else {
                        build(jsc[index], caption_neg, img_neg);
                        build(jsc[index], caption_neg, img_neg);
                    }
                })
            });
        }
    })
}

window.addEventListener('DOMContentLoaded', function() {
    json_take();
});

//изменение характеристик ЭЗС
$('[name="charger_selector"]').click(function() {
    plug = $('input[name="charger_selector"]:checked').val();
    plug_path = '/frontend/images/'+'m_'+plug+'.png';
    json_take();
});

$('#ac, #dc, #fromInput_kvt, #toInput_kvt, ' +
  '#fromInput_price, #toInput_price, ' +
  '#fromSlider_kvt, #toSlider_kvt, ' +
  '#fromSlider_price, #toSlider_price').on('change', function() {
    ac_dc = '('+eletypize()+')';
    ac_dc = ac_dc.replace('ac', '"ac"')
                 .replace('dc','"dc"');
    from_power = $('#fromInput_kvt').val();
    to_power = $('#toInput_kvt').val();
    from_price = $('#fromInput_price').val();
    to_price = $('#toInput_price').val();
    json_take();
});


function route_id(idsi) {
    let route_stat = [starti]
    $.each(idsi, function (index, value) {
        let idf = jsc.findIndex(obj => obj.id == value);
        let next_point = {type: 'viaPoint',
                          point: [jsc[idf].lat, jsc[idf].lon]};

        route_stat.push(next_point);
    });
    route_stat.push(finishi)
    return route_stat
}

//построение оптимального маршрута
let starti;
let finishi;
function booking_init(idsi) {
    starti = [start.split(',')[0], start.split(',')[1]]
    finishi = [finish.split(',')[0], finish.split(',')[1]]
    let myMap = new ymaps.Map("map", {
        center: starti,
        zoom: 13
    });
    ymaps.route(route_id(idsi), {
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
            let idf = jsc.findIndex(obj => obj.id == value)
            MyIconContentLayout = ymaps.templateLayoutFactory.createClass(
                '<div>$[properties.iconContent]</div>'
            );
            let capt = '№ ' + String(jsc[idf].id) + '<br/>' +
                '---------------------------' + '<br/>' +
                'Адрес: ' + String(jsc[idf].address) + '<br/>' +
                'Компания: ' + String(jsc[idf].company) + '<br/>' +
                'Тип тока: ' + String(jsc[idf].plug_type).toUpperCase() + tokize(jsc[idf].plug_type) + '<br/>' +
                'Мощность: '+ String(jsc[idf].power).toUpperCase() + " кВт" + '<br/>' +
                '<img src=' + '"' + plug_path + '"' + '</img>' + '<br/>' +
                '---------------------------' + '<br/>' +
                String(jsc[idf].price) + ' руб. за 1 кВт';
            myPlacemarkWithContent = new ymaps.Placemark([jsc[idf].lat, jsc[idf].lon], {
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
    booking_init(idsi);
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
function map_display(){
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
            if (ids.length == 0){
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
    let data = {jsc: jsc,
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
            if (ids == 'impossible'){
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
                map_display();
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