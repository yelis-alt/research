$('#ok').click(function(){
    preventSend();
    if (($('.ymaps-2-1-79-route-panel-input__input').eq(0).val() != '') &
        ($('.ymaps-2-1-79-route-panel-input__input').eq(1).val() != '')){
        getRoute()
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

$('#window_repeat').click(function() {
    $('#register').on('submit', function (event) {
        event.preventDefault();
    });
    $('#mapi').attr("id","map");
    $(".mapi__land-inner").attr("class", '.ymaps-2-1-79-map.' +
        'ymaps-2-1-79-i-ua_js_yes.' +
        'ymaps-2-1-79-map-bg.' +
        'ymaps-2-1-79-islets_map-lang-ru');
    $('.booking').hide();
    $('#map').hide();
    dropMin();
    getRoute();
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

function dropMin(){
    let comWin = {}
    $.each(ids, function(index, value){
        comWin[value] = 0;
    });
    $.each(indexWindows, function(index, value){
        let idt = value.split('_')[1];
        comWin[idt] += 1;
    });
    let minWin = 100000000;
    let minEl;
    $.each(Object.keys(comWin), function(index, value){
        if (comWin[value] < minWin){
            minWin = comWin[value];
            minEl = value;
        }
    });
    let idDel = stationsList.findIndex(obj => obj.id === minEl);
    stationsList.splice(idDel, 1);
}

function getRoute(){
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
                displayBookingPanel();
                displayBooking();
                extend();
                displayMap();
            }
        }
    });
}