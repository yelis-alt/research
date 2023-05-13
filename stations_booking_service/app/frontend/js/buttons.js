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
         $('#dc').is(':checked')) == 0){
        $('#dc').prop('checked', true);
    }
});

$('#dc').change(function(){
    if (($('#ac').is(':checked') +
         $(this).is(':checked')) == 0){
        $('#ac').prop('checked', true);
    }
});

$('.parameter__temp-acc').change(function(){
    if ($(this).val() == '') {
        $(this).val('0');
    }
});

$('.parameter__temp-maxacc').change(function(){
    if ($(this).val() == '') {
        $(this).val('0');
    }
});

$('.parameter__temp-spend').change(function(){
    if ($(this).val() == '') {
        $(this).val('0');
    }
});

$('#select-city').change(function(){
    let tar = $(this).val();
    lati = centers[tar][0]
    long = centers[tar][1]
    init();
    json_take();
    })