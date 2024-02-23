let currentId;
let posId;
let nextId;
let resp;

let scheduleMap = {};
let windowsList = [];
let availWinMap = {};
let indexWindowsList = [];
let subWinMap = {}

let cell = '.booking';
let id = $('.booking__station input[type=number]');
let input = $('.booking');

input.change(function() {
    $(indexWindowsList).each(function(index, value) {
        if (input.eq(index).is(':checked')) {
            $(cell+value.toString()).css({
                'opacity': '1',
                'border': 'solid rgba(0, 0, 0, 1) 2px',
            });
        }else{
            $(cell+value.toString()).css({
                'opacity': '0.6',
                'border': 'solid rgba(0, 0, 0, 0.6) 2px',
            });
        }
    });
});

$('.booking__station-back').click(function(){
    currentId = id.val();
    posId = $.inArray(parseInt(currentId), stationIdsList);
    if (posId === 0) {
        if (stationIdsList.length !== 1) {
        nextId = $(stationIdsList).get(-1);
        id.val(nextId);
        extend();
        }
    }
    else {
        id.val($(stationIdsList).get(posId-1));
        extend();
    }

    slideWindows();
});

$('.booking__station-forward').click(function(){
    currentId = id.val();
    posId = $.inArray(currentId, stationIdsList);
    if (posId === $(stationIdsList).length-1){
        nextId = $(stationIdsList).get(0);
        id.val(nextId);
        extend();
    }
    else{
        id.val($(stationIdsList).get(posId+1));
        extend();
    }
    slideWindows();
});


$('#register').on('submit', function (event) {
    event.preventDefault();
});

$('#window_send').click(function() {
    if ($('.booking__period input:checked').length === 0) {
        alert('Выберите хотя бы одно окно брони');
    } else {
        endWindow();
        $.ajax({
            type: 'POST',
            url: 'schedule',
            data: JSON.stringify({resp: resp}),
            dataType : 'json',
            contentType: 'application/json',
            error: function () {
                setTimeout(function(){
                    alert('К сожалению, не удалось зарегистрировать Вашу бронь');
                }, 1000);
            },
            success: function () {
                setTimeout(function(){
                    $('#register').unbind('submit').submit();
                    alert('Ваш уникальный логин брони: ' + login);
                }, 1000);
            }
        })
    }
});

function getTimeWindows(){
    let timeWindowsMapRequest =
        {
            "date": getDateString(getDateString($("#trip_date").val())),
            stationIdsList: stationIdsList
        }
    $.ajax({
        type:'POST',
        url: 'http://localhost:8080/schedule/getTimeWindows',
        dataType : 'json',
        contentType: 'application/json',
        data: JSON.stringify(timeWindowsMapRequest),
        success: function(data){
            $.each(data, function(key, value) {
                scheduleMap[key] = value;
            });
        }
    });
}

function displayBooking(){
    $('#loading').hide();
    $('#map').show();
    $('.ymaps-2-1-79-map').remove();
    initializeBooking(stationIdsList);
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

function trail_null(word){
    if (word < 10){
        word = '0' + word;
    }
    return word
}
function add20s(setMonth, setDay, setYear,
                setHour, fixDate, step, resid) {
    let nextDate = new Date(fixDate +
        60000*(20*step  - resid));
    let endDate = new Date(fixDate +
        60000*(20*(step+1)  - resid));
    let month = String(nextDate.getUTCMonth() + 1);
    let day = String(nextDate.getUTCDate());
    let year = String(nextDate.getUTCFullYear());
    let cHour = String(nextDate.getHours());
    let cMin = String(nextDate.getMinutes());
    let eHour = String(endDate.getHours());
    let eMin = String(endDate.getMinutes());
    let fullDate = trail_null(day) + "." + trail_null(month) + "." +
        trail_null(year);
    let fullTime = trail_null(cHour)+ ":" + trail_null(cMin);
    let endTime = trail_null(eHour)+ ":" + trail_null(eMin);
    if ((Number(setDay) === Number(day)) &&
        (Number(setMonth) === Number(month)) &&
        (Number(setYear) === Number(year)) &&
        (Number(setHour) > Number(cHour))){
        return '-'
    }else{
        return fullDate + ' ' + fullTime + '-' + endTime
    }
}

function setWindows(){
    let minut = new Date(Date.now());
    let resid = minut.getMinutes() % 20;
    let fixDate = Date.now();
    let compDate = new Date(Date.now());
    let setMonth = String(compDate.getUTCMonth() + 1);
    let setDay = String(compDate.getUTCDate());
    let setYear = String(compDate.getUTCFullYear());
    let setHour = String(compDate.getHours());
    for (let step = 0; step<=((24*60)/20*14); step++) {
        let dCell = add20s(setMonth, setDay, setYear,
                            setHour, fixDate, step, resid);
        if (dCell !== '-') {
            windowsList.push(dCell)
        }
    }
    windowsList.sort();
}

function drawWindows(){
    $.each(Object.keys(availWinMap), function(index1, value1){
        let preIndex = value1
        $.each(availWinMap[preIndex], function(index2, value2){
            let afterIndex = '_' + String(preIndex) +
                             '_' + String(index2);
            indexWindowsList.push(afterIndex);
            let id_name = 'window' + afterIndex;

            $('.booking__period').append('<input type="checkbox" id=' + '"' + id_name +
                '" ' +'value=' + '" ' + value2 + '"'+ '/>');
            $('.booking__period').append('<label class="booking__period-'+
                id_name + '"' + ' for=' + '"'
                + id_name + '"> ' + '&nbsp;' + value2.split(' ')[0] +
                '<br>' + value2.split(' ')[1] + '</label>');
        })
    });
}

function filterWindows(){
    $.each(stationIdsList, function(index1, value1){
        let chanWin = windowsList;

        $.each(scheduleMap[value1], function(index2, value2){
            if ($.inArray(value2, windowsList) !== -1){
                chanWin = chanWin.filter(function(e){
                    return e!==value2;
                });
            }
        });

        availWinMap[value1] = chanWin;
    });
}

function slideWindows(){
    let cid = String(id.val());
    $.each(indexWindowsList, function(index, value){
        if (value.split('_')[1] === cid){
            $(cell+value).css({
                'display': 'block'
            });
        }else{
            $(cell+value).css({
                'display': 'none'
            })
        }
    });
}

function endWindow(){
    subWinMap[login] = {};
    $.each(stationIdsList, function(index, value){
        subWinMap[login][value] = []
    });
    $.each(indexWindowsList, function(index, value) {
        let checkId = input.eq(index).attr('id');
        if (input.eq(index).is(':checked')) {
            let checkVal =  $('#'+checkId).val();
            let checkInd = checkId.split('_')[1];
            subWIn[login][checkInd].push(checkVal.slice(1));
        }
    });
    $.each(stationIdsList, function(index, value){
        if (subWIn[login][value].length === 0) {
            delete subWIn[login][value];
        }
    })
    let keyResp = Object.keys(resp);
    let keySub = Object.keys(subWIn[login]);
    $.each(keySub, function(index, value){
        if ($.inArray(value, keyResp) !== -1){
            resp[value][login] = subWIn[login][value];
        }else{
            resp[value] = {};
            resp[value][login] = subWIn[login][value];
        }
    })
}

function displayBookingPanel(){
    setWindows();
    filterWindows();
    drawWindows();
    cell = '.booking__period-window';
    input = $('.booking__period input');
    slideWindows();
}

function extend(){
    let digits = id.val().toString().length;
    let wid =  Number(digits)*10;
    id.css({
        'width': wid
    });
}
