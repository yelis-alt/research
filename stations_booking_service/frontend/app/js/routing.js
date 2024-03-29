let routeResponse;
$("#ok").click(function(){
    preventSend();
    if (($(".ymaps-2-1-79-route-panel-input__input").eq(0).val() != "") &
        ($(".ymaps-2-1-79-route-panel-input__input").eq(1).val() != "")){
        getRoute()
    }else{
        if (($(".ymaps-2-1-79-route-panel-input__input").eq(0).val() == "") &
            ($(".ymaps-2-1-79-route-panel-input__input").eq(1).val() == "")) {
            alert("Пожалуйста, выберите пункты отправления и прибытия");
            preventSend();
        }else{
            if ($(".ymaps-2-1-79-route-panel-input__input").eq(0).val() == "") {
                alert("Пожалуйста, выберите пункт отправления");
                preventSend();
            }else{
                alert("Пожалуйста, выберите пункт прибытия");
                preventSend();
            }
        }
    }
});

$("#window_repeat").click(function() {
    $("#register").on("submit", function (event) {
        event.preventDefault();
    });
    $("#mapi").attr("id","map");
    $(".mapi__land-inner").attr("class", ".ymaps-2-1-79-map." +
        "ymaps-2-1-79-i-ua_js_yes." +
        "ymaps-2-1-79-map-bg." +
        "ymaps-2-1-79-islets_map-lang-ru");
    $(".booking").hide();
    $("#map").hide();

    dropMin();
    getRoute();
});

function getRouteId(stationIdsList) {
    let routeStat = [startCoordsBooking];
    $.each(stationIdsList, function (index, value) {
        let stationIndex = stationsList.findIndex(station => station.id === value);
        let nextPoint =
            {type: "viaPoint",
            point: [stationsList[stationIndex].longitude, stationsList[stationIndex].latitude]};

        routeStat.push(nextPoint);
    });
    routeStat.push(finishCoordsBooking);

    return routeStat;
}

function dropMin(){
    let comWin = {}
    $.each(stationIdsList, function(index, value){
        comWin[value] = 0;
    });
    $.each(indexWindowsList, function(index, value){
        let idt = value.split("_")[1];
        comWin[idt] += 1;
    });
    let minWin = 100;
    let minEl;
    $.each(Object.keys(comWin), function(index, value){
        if (comWin[value] < minWin){
            minWin = comWin[value];
            minEl = value;
        }
    });
    let posDel = stationsList.findIndex(obj => obj.id === parseInt(minEl));
    stationsList.splice(posDel, 1);
}

function getRoute(){
    let accLevel = $(".parameter__temp-acc").val();
    let accMax = $(".parameter__temp-maxacc").val();
    let spendOpt = $(".parameter__temp-spend").val();
    let temperature = $(".weather__temp-number").val();
    let routeRequest = {
        startCoords: startCoords,
        finishCoords: finishCoords,
        accLevel: parseInt(accLevel),
        accMax: parseFloat(accMax).toFixed(1),
        spendOpt: parseFloat(spendOpt).toFixed(1),
        temperature: parseFloat(temperature).toFixed(1),
        filteredStationsList: stationsList};
    $(".ymaps-2-1-79-route-panel__clear").click();
    $(".tabs").hide();
    $(".ymaps-2-1-79-controls__control_toolbar").hide();
    $(".routing__legendbox").hide();
    $(".ymaps-2-1-79-zoom").hide();
    $("#ok").hide();
    $("#map").hide();
    $("#loading").show();
    $(".textbox").css({
        "margin-top": "3.25px"
    });
    $.ajax({
        type:"POST",
        url: HOST_JAVA + "routing/getRoute",
        dataType : "json",
        contentType: "application/json",
        data: JSON.stringify(routeRequest),
        success: function(response){
            routeResponse = response;
            if (response.length === 0){
                alert("К сожалению, согласно заданными Вами параметрами " +
                    "построение маршрута невозможно.\n" +
                    "Вы можете поменять их и попробовать снова.");
                window.location.reload();
            } else {
                stationNodesList = response.slice(1, -1);
                stationIdsList = [];
                if (stationNodesList.length !== 0) {
                    $(stationNodesList).each(function (ind) {
                        stationIdsList.push(stationNodesList[ind].routeNode.id)
                    });
                    $(".booking__station input[type=number]").val(stationIdsList[0]);
                } else {
                    $(".booking__station input[type=number]").val("-");
                }
                displayBookingPanel();
                displayBooking();
                extend();
                displayMap();
            }
        },
        error: function () {
            alert("К сожалению, не удалось построить Ваш маршрут");
            window.location.reload();
        }
    });
}
