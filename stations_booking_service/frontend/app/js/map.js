ymaps.ready(init);

let start;
let finish;
let starti;
let finishi;
let ids;
let login;
let stationsList;

let imgPos = '/images/c.png';
let imgNeg = '/images/d.png';
let plug = 'TYPE_2';
let plugPath = '/images/m_type2.png';
let plugType = ["AC", "DC"];
let fromPower = 10;
let toPower = 50;
let fromPrice = 0;
let toPrice = 60;
let geoObj = [];

window.addEventListener('DOMContentLoaded', function() {
    getStations();
});

$('[name="charger_selector"]').click(function() {
    plug = $('input[name="charger_selector"]:checked').val();
    plugPath = '/images/'+'m_'+
        plug.replace("_", "").toLowerCase()+
        '.png';
    getStations();
});

$('#ac, #dc, #fromInput_kvt, #toInput_kvt, ' +
    '#fromInput_price, #toInput_price, ' +
    '#fromSlider_kvt, #toSlider_kvt, ' +
    '#fromSlider_price, #toSlider_price').on('change', function() {
    plugType = getPlugTypesList();
    fromPower = $('#fromInput_kvt').val();
    toPower = $('#toInput_kvt').val();
    fromPrice = $('#fromInput_price').val();
    toPrice = $('#toInput_price').val();
    getStations();
});

function getCoordinates(desc, n, p) {
    return desc["properties"]["waypoints"][n]["coordinates"][p].toString();
}

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
                start = getCoordinates(desc, 0, 1) + ',' + getCoordinates(desc, 0, 0);
                finish = getCoordinates(desc, 1, 1) + ',' + getCoordinates(desc, 1, 0);
            }
        });
    });
}

function build(ind, caption, img){
    for (let i = 0; i < 2 ; i++) {
        myPlacemarkWithContent = new ymaps.Placemark([ind.longitude, ind.latitude], {
            balloonContent: caption
        }, {
            iconLayout: 'default#imageWithContent',
            iconImageHref: img,
            iconImageSize: [25, 25],
            iconImageOffset: [-10, -10],
            iconContentOffset: [-8, 10],
            iconContentLayout: MyIconContentLayout
        });

        console.log(myPlacemarkWithContent)
        myMap.geoObjects.add(myPlacemarkWithContent);
        geoObj.push(myPlacemarkWithContent);
    }

}

function getPlugTypesList(){
    let plugTypesList = [];
    let plugTypesSelect = $('#ac_dc input[type=checkbox]:checked');
    for (let i = 0; i < plugTypesSelect.length; i++) {
        plugTypesList.push(plugTypesSelect.eq(i).val());
    }

    return plugTypesList;
}

function getPlugTypeDesc(str) {
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
            fromPower: parseInt(fromPower),
            toPower: parseInt(toPower),
            fromPrice: parseInt(fromPrice),
            toPrice: parseInt(toPrice)
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
                    if (stationsList[index].status === true) {
                        let caption = '№' + String(stationsList[index].id) + '<br/>' +
                            '---------------------------' + '<br/>' +
                            'Адрес: ' + String(stationsList[index].address) + '<br/>' +
                            'Компания: ' + String(stationsList[index].company) + '<br/>' +
                            'Тип тока: ' + String(stationsList[index].plugType).toUpperCase() + getPlugTypeDesc(stationsList[index].plugType) + '<br/>' +
                            'Мощность: ' + String(stationsList[index].power).toUpperCase() + " кВт" + '<br/>' +
                            '<img src=' + '"' + plugPath + '"' + '</img>' + '<br/>' +
                            '---------------------------' + '<br/>' +
                            String(stationsList[index].price) + ' руб. за 1 кВт';
                        build(stationsList[index], caption, imgPos);
                    } else {
                        let caption = '№' + String(stationsList[index].id) + '<br/>' +
                            '---------------------------' + '<br/>' + 'ЭЗС временно недоступна';
                        build(stationsList[index], caption, imgNeg);
                    }
                })
            });
        }
    })
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
            iconImageHref: '/images/a.png',
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
                'Тип тока: ' + String(stationsList[idf].plugType).toUpperCase() + getPlugTypeDesc(stationsList[idf].plugType) + '<br/>' +
                'Мощность: '+ String(stationsList[idf].power).toUpperCase() + " кВт" + '<br/>' +
                '<img src=' + '"' + plugPath + '"' + '</img>' + '<br/>' +
                '---------------------------' + '<br/>' +
                String(stationsList[idf].price) + ' руб. за 1 кВт';
            myPlacemarkWithContent = new ymaps.Placemark([stationsList[idf].latitude, stationsList[idf].longitude], {
                balloonContent: capt
            }, {
                iconLayout: 'default#imageWithContent',
                iconImageHref: '/images/c.png',
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
            iconImageHref: '/images/b.png',
            iconImageSize: [25, 25],
            iconImageOffset: [-10, -10],
            iconContentOffset: [-8, 10],
            iconContentLayout: MyIconContentLayout
        }));
    }
}
