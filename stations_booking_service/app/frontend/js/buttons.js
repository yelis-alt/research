let now = new Date();
let now_day = ("0" + now.getDate()).slice(-2);
let now_month = ("0" + (now.getMonth() + 1)).slice(-2);
let now_date = now.getFullYear()+"-"+(now_month)+"-"+(now_day) ;
$('#trip_date').val(now_date);
$('#trip_date').attr({"min": now_date});

let future = now;
future.setDate(future.getDate()+14);
let future_day = ("0" + future.getDate()).slice(-2);
let future_month = ("0" + (future.getMonth() + 1)).slice(-2);
let future_date = future.getFullYear()+"-"+(future_month)+"-"+(future_day) ;
$('#trip_date').attr({"max": future_date});

function preventSend(){
    $('#submit').on('submit', function(event){
        event.preventDefault();
    });
}

$('.city-select').change(function(){
    let text = $(this).find('option:selected').text();
    let aux = $('<select/>').append($('<option/>').text(text));
    $(this).after(aux);
    $(this).width(aux.width()*1.75);
    aux.remove();
}).change();

$(function(){
    let requiredCheckboxes = $('.type_electricity');
    requiredCheckboxes.change(function(){
        if(requiredCheckboxes.is(':checked')) {
        } else {
            $('#ac').prop('checked', true);
            $('#dc').prop('checked', true);
        }
    });
});

$('#ac').change(function(){
    if (($(this).is(':checked') +
         $('#dc').is(':checked')) === 0){
        $('#dc').prop('checked', true);
    }
});

$('#dc').change(function(){
    if (($('#ac').is(':checked') +
         $(this).is(':checked')) === 0){
        $('#ac').prop('checked', true);
    }
});

$('.parameter__temp-acc').change(function(){
    let size_acc  = $('.parameter__temp-acc').val().length;
    if (size_acc === 1) {
        $(".parameter__temp-acc").css('width', 19);
    }

    if (size_acc === 2) {
        $(".parameter__temp-acc").css('width', 32);
    }

    if (size_acc === 3) {
        $(".parameter__temp-acc").css('width', 42);
    }
});

$('.parameter__temp-maxacc').change(function(){
    let size_maxacc  = $('.parameter__temp-maxacc').val().length;
    if (size_maxacc === 1) {
        $(".parameter__temp-maxacc").css('width', 19);
    }

    if (size_maxacc === 2) {
        $(".parameter__temp-maxacc").css('width', 32);
    }

    if (size_maxacc === 3) {
        $(".parameter__temp-maxacc").css('width', 42);
    }
});

$('.parameter__temp-spend').change(function(){
    let size_spend  = $('.parameter__temp-spend').val().length;
    if (size_spend === 1) {
        $(".parameter__temp-spend").css('width', 19);
    }

    if (size_spend === 2) {
        $(".parameter__temp-spend").css('width', 32);
    }

    if (size_spend === 3) {
        $(".parameter__temp-spend").css('width', 42);
    }
});

$('#select-city').change(function(){
    let tar = $(this).val();
    lati = centers[tar][0]
    long = centers[tar][1]
    init();
    json_take();
    })