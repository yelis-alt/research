let now = new Date();
let nowDay = ("0" + now.getDate()).slice(-2);
let nowMonth = ("0" + (now.getMonth() + 1)).slice(-2);
let nowDate = now.getFullYear()+"-"+(nowMonth)+"-"+(nowDay) ;
$('#trip_date').val(nowDate);
$('#trip_date').attr({"min": nowDate});

let future = now;
future.setDate(future.getDate()+10);
let futureDay = ("0" + future.getDate()).slice(-2);
let futureMonth = ("0" + (future.getMonth() + 1)).slice(-2);
let futureDate = future.getFullYear()+"-"+(futureMonth)+"-"+(futureDay) ;
$('#trip_date').attr({"max": futureDate});

function preventSend(){
    $('#submit').on('submit', function(event){
        event.preventDefault();
    });
}

$('.weather__temp-date-picker').change(function(){
    $(this).css('padding-top', 6);
});

$('.city-select').change(function(){
    let text = $(this).find('option:selected').text();
    let aux = $('<select/>').append($('<option/>').text(text));
    $(this).after(aux);
    $(this).width(aux.width()*1.75);
    aux.remove();
}).change();

$('.model-select').change(function(){
    let text = $(this).find('option:selected').text();
    let aux = $('<select/>').append($('<option/>').text(text));
    $(this).after(aux);
    $(this).width(aux.width()*1.15);
    aux.remove();

    if ($(this).find(":selected").val()  === "volkswagen_id_4") {
        $('.parameter__temp-maxacc').val("77");
        $('.parameter__temp-spend').val("17");
    }
    if ($(this).find(":selected").val() === "evolute-i-pro") {
        $('.parameter__temp-maxacc').val("53");
        $('.parameter__temp-spend').val("12.6");
    }

    let sizeMaxacc  = $('.parameter__temp-maxacc').val().length;
    if (sizeMaxacc === 1) {
        $(".parameter__temp-maxacc").css('width', 19);
    }
    if (sizeMaxacc === 2) {
        $(".parameter__temp-maxacc").css('width', 32);
    }
    if (sizeMaxacc === 3) {
        $(".parameter__temp-maxacc").css('width', 42);
    }
    if (sizeMaxacc === 4) {
        $(".parameter__temp-maxacc").css('width', 45);
    }
    let sizeSpend  = $('.parameter__temp-spend').val().length;
    if (sizeSpend === 1) {
        $(".parameter__temp-spend").css('width', 19);
    }

    if (sizeSpend === 2) {
        $(".parameter__temp-spend").css('width', 32);
    }

    if (sizeSpend === 3) {
        $(".parameter__temp-spend").css('width', 42);
    }
    if (sizeSpend === 4) {
        $(".parameter__temp-spend").css('width', 45);
    }
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
    let sizeAcc  = $('.parameter__temp-acc').val().length;
    if (sizeAcc === 1) {
        $(this).css('width', 30);
    }

    if (sizeAcc === 2) {
        $(this).css('width', 40);
    }

    if (sizeAcc === 3) {
        $(this).css('width', 50);
    }
});

$('.parameter__temp-maxacc').change(function(){
    let sizeMaxacc  = $('.parameter__temp-maxacc').val().length;
    if (sizeMaxacc === 1) {
        $(this).css('width', 19);
    }
    if (sizeMaxacc === 2) {
        $(this).css('width', 32);
    }
    if (sizeMaxacc === 3) {
        $(this).css('width', 42);
    }
    if (sizeMaxacc === 4) {
        $(this).css('width', 45);
    }
});

$('.parameter__temp-spend').change(function(){
    let sizeSpend  = $('.parameter__temp-spend').val().length;
    if (sizeSpend === 1) {
        $(this).css('width', 19);
    }

    if (sizeSpend === 2) {
        $(this).css('width', 32);
    }

    if (sizeSpend === 3) {
        $(this).css('width', 42);
    }
    if (sizeSpend === 4) {
        $(this).css('width', 45);
    }
});

$('#select-city').change(function(){
    let tar = $(this).val();
    lati = centers[tar][0]
    long = centers[tar][1]
    init();
    getStations();
    })