function trail_null(word){
    if (word < 10){
        word = '0' + word;
    }
    return word
}
function add20s(set_month, set_day, set_year,
                set_hour, fixDate, step, resid) {
    let next_date = new Date(fixDate +
        60000*(20*step  - resid));
    let end_date = new Date(fixDate +
        60000*(20*(step+1)  - resid));
    let month = String(next_date.getUTCMonth() + 1);
    let day = String(next_date.getUTCDate());
    let year = String(next_date.getUTCFullYear());
    let cHour = String(next_date.getHours());
    let cMin = String(next_date.getMinutes());
    let eHour = String(end_date.getHours());
    let eMin = String(end_date.getMinutes());
    let full_date = trail_null(day) + "." + trail_null(month) + "." +
        trail_null(year);
    let full_time = trail_null(cHour)+ ":" + trail_null(cMin);
    let end_time = trail_null(eHour)+ ":" + trail_null(eMin);
    if ((Number(set_day) == Number(day)) &&
        (Number(set_month) == Number(month)) &&
        (Number(set_year) == Number(year)) &&
        (Number(set_hour) > Number(cHour))){
        return '-'
    }else{
        return full_date + ' ' + full_time + '-' + end_time
    }
}

function windows_set(){
    let minut = new Date(Date.now());
    let resid = minut.getMinutes() % 20;
    let fixDate = Date.now();
    let compDate = new Date(Date.now());
    let set_month = String(compDate.getUTCMonth() + 1);
    let set_day = String(compDate.getUTCDate());
    let set_year = String(compDate.getUTCFullYear());
    let set_hour = String(compDate.getHours());
    for (let step = 0; step<=((24*60)/20*14); step++) {
        let d_cell = add20s(set_month, set_day, set_year,
                            set_hour, fixDate, step, resid);
        if (d_cell != '-') {
            windows.push(d_cell)
        }
    }
    windows.sort();
}

function windows_draw(){
    $.each(Object.keys(avail_win), function(index1, value1){
        let preindex = value1
        $.each(avail_win[preindex], function(index2, value2){
            let afterindex = '_' + String(preindex) +
                             '_' + String(index2);
            index_windows.push(afterindex);
            let id_name = 'window' + afterindex;

            $('.booking__period').append('<input type="checkbox" id=' + '"' + id_name +
                '" ' +'value=' + '" ' + value2 + '"'+ '/>');
            $('.booking__period').append('<label class="booking__period-'+
                id_name + '"' + ' for=' + '"'
                + id_name + '"> ' + '&nbsp;' + value2.split(' ')[0] +
                '<br>' + value2.split(' ')[1] + '</label>');
        })
    });
}

function json_ids(){
    let url = './schedule.json'
    fetch(url)
        .then(response => response.json())
        .then(data => {
            resp = data;
            let list_ids = Object.keys(resp);
            const flattenJSON = (obj = {}, res = {}, extraKey = '') => {
                for(key in obj){
                    if(typeof obj[key] !== 'object'){
                        res[extraKey + key] = obj[key];
                    }else{
                        flattenJSON(obj[key], res, `${extraKey}${key}.`);
                    }
                }
                return res;
            };
            let temp_schedule = flattenJSON(resp);
            $.each(list_ids, function(index, value){
                schedule[value] = [];
                for (key in temp_schedule){
                    let flat_id = key.split('.')[0]
                    if (flat_id  == value){
                        schedule[value].push(temp_schedule[key])
                    }
                }
            });
        });
}

function window_filter(){
    $.each(ids, function(index1, value1){
        let chan_win = windows;
        $.each(schedule[value1], function(index2, value2){
            if ($.inArray(value2, windows) != -1){
                chan_win = chan_win.filter(function(e){
                    return e!=value2;
                });
            }
        });
        avail_win[value1] = chan_win;
    });
}

function window_slide(){
    let cid = String(id.val());
    $.each(index_windows, function(index, value){
        if (value.split('_')[1] == cid){
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

function window_end(){
    sub_win[login] = {};
    $.each(ids, function(index, value){
        sub_win[login][value] = []
    });
    $.each(index_windows, function(index, value) {
        let check_id = input.eq(index).attr('id');
        if (input.eq(index).is(':checked')) {
            let check_val =  $('#'+check_id).val();
            let check_ind = check_id.split('_')[1];
            sub_win[login][check_ind].push(check_val.slice(1));
        }
    });
    $.each(ids, function(index, value){
        if (sub_win[login][value].length == 0) {
            delete sub_win[login][value];
        }
    })
    let key_resp = Object.keys(resp);
    let key_sub = Object.keys(sub_win[login]);
    $.each(key_sub, function(index, value){
        if ($.inArray(value, key_resp) != -1){
            resp[value][login] = sub_win[login][value];
        }else{
            resp[value] = {};
            resp[value][login] = sub_win[login][value];
        }
    })
}

let windows = [];
let resp;
let schedule = {};
let avail_win = {};
let index_windows = [];
let sub_win = {}
json_ids();
function booking_panel(){
    windows_set();
    window_filter();
    windows_draw();
    cell = '.booking__period-window';
    input = $('.booking__period input');
    window_slide();
}

let cell = '.booking';
let input = $('.booking');
input.change(function() {
    $(index_windows).each(function(index, value) {
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

let id = $('.booking__station input[type=number]');
function extender(){
    let digits = id.val().toString().length;
    let wid =  Number(digits)*10;
    id.css({
        'width': wid
    });
}

let current_id;
let pos_id;
let next_id;
$('.booking__station-back').click(function(){
    current_id = id.val();
    pos_id = $.inArray(current_id, ids);
    if (pos_id == 0){
        next_id = $(ids).get(-1);
        id.val(next_id);
        extender();
    }
    else{
        id.val($(ids).get(pos_id-1));
        extender();
    }
    window_slide();
});

$('.booking__station-forward').click(function(){
    current_id = id.val();
    pos_id = $.inArray(current_id, ids);
    if (pos_id == $(ids).length-1){
        next_id = $(ids).get(0);
        id.val(next_id);
        extender();
    }
    else{
        id.val($(ids).get(pos_id+1));
        extender();
    }
    window_slide();
});

function min_drop(){
    let com_win = {}
    $.each(ids, function(index, value){
        com_win[value] = 0;
    });
    $.each(index_windows, function(index, value){
        let idt = value.split('_')[1];
        com_win[idt] += 1;
    });
    let min_win = 100000000;
    let min_el;
    $.each(Object.keys(com_win), function(index, value){
        if (com_win[value] < min_win){
            min_win = com_win[value];
            min_el = value;
        }
    });
    let id_del = jsc.findIndex(obj => obj.id == min_el);
    jsc.splice(id_del, 1);
}

$('#register').on('submit', function (event) {
    event.preventDefault();
});

$('#window_send').click(function() {
    if ($('.booking__period input:checked').length == 0) {
        alert('Выберите хотя бы одно окно брони');
    } else {
        window_end();
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
    min_drop();
    solution();
});