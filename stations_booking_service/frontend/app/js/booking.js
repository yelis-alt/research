let currentId;
let posId;
let nextId;
let scheduleList;

let HOST_JAVA = "http://localhost:8080/"
let REACH_TIME_DESC = "•  Время прибытия:";
let CHARGE_TIME_DESC = "•  Продолжительность зарядки:";
let FULL_TIME_DESC = "*Продолжительность маршрута:";
let WINDOW_PREFIX = "#window"
let BOOKING_PERIOD = ".booking__period-"
let CELL_SLIDE = ".booking__period-window";

let windowsList = [];
let availWindowsList = [];
let indexWindowsList = [];

let id = $(".booking__station input[type=number]");
let input = $(".booking");

input.change(function() {
    $(indexWindowsList).each(function(index, value) {
        let windowId = WINDOW_PREFIX + value;
        let windowClass = BOOKING_PERIOD + windowId.slice(1);
        if ($(windowId).is(":checked")) {
            $(windowClass).css({
                "opacity": "1",
                "border": "solid rgba(0, 0, 0, 1) 2px",
            });
        } else {
            $(windowClass).css({
                "opacity": "0.6",
                "border": "solid rgba(0, 0, 0, 0.6) 2px",
            });
        }
    });
});

$(".booking__station-back").click(function(){
    currentId = parseInt(id.val());
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

$(".booking__station-forward").click(function(){
    currentId = parseInt(id.val());
    posId = $.inArray(currentId, stationIdsList);
    if (posId === $(stationIdsList).length - 1) {
        if (stationIdsList.length !== 1) {
            nextId = $(stationIdsList).get(0);
            id.val(nextId);
            extend();
        }
    } else {
        id.val($(stationIdsList).get(posId+1));
        extend();
    }
    slideWindows();
});


$("#register").on("submit", function (event) {
    event.preventDefault();
});

$("#window_send").click(function() {
    let bookedWindowsList = $(".booking__period input:checked");
    let code = getCode();
    if (bookedWindowsList.length === 0) {
        alert("Выберите хотя бы одно окно брони");
    } else {
        let bookedWindowsMap = {};
        for (let i = 0; i < bookedWindowsList.length; i++) {
            let stationId = bookedWindowsList[i].id.split("_")[1];
            let keysList = Object.keys(bookedWindowsMap);
            if (keysList.indexOf(stationId) === -1) {
                bookedWindowsMap[stationId] = [];
            }

            let timeWindowRaw = bookedWindowsList[i].defaultValue.slice(1);
            let timeWindowSplit = timeWindowRaw.split(" ");
            let dateStringSplit = timeWindowSplit[0].split(".");
            let dateRef = dateStringSplit[2] + "-" + dateStringSplit[1] + "-" +
                dateStringSplit[0];
            let timeRef = timeWindowSplit[1];
            let timeWindow = dateRef + " " + timeRef;

            bookedWindowsMap[stationId].push(timeWindow);
        }

        let saveWindowsRequestsList = [];
        let keysList = Object.keys(bookedWindowsMap);
        $.each(keysList, function (index, key) {
            let saveWindowRequest =
                {
                    "stationId": parseInt(key),
                    "code": code,
                    "timeWindowsList": bookedWindowsMap[key]
                }

                saveWindowsRequestsList.push(saveWindowRequest);
        });

        $.ajax({
            type: "POST",
            url: HOST_JAVA + "schedule/saveTimeWindows",
            data: JSON.stringify(saveWindowsRequestsList),
            dataType: "json",
            contentType: "application/json",
            success: function () {
                alert("Ваш уникальный логин брони: " + code);
                location.reload(true);
            },
            error: function () {
                alert("К сожалению, не удалось зарегистрировать Вашу бронь");
            }
        });
    }
});

function getTimeWindows(){
    let timeWindowsMapRequest =
        {
            "date": getDateString(getDateString($("#trip_date").val())),
            stationIdsList: stationIdsList
        }
    $.ajax({
        type:"POST",
        url: HOST_JAVA + "schedule/getTimeWindows",
        dataType : "json",
        contentType: "application/json",
        data: JSON.stringify(timeWindowsMapRequest),
        success: function(response){
            scheduleList = response;
            filterWindows();
        }
    });
}

function displayBooking(){
    $("#loading").hide();
    $("#map").show();
    $(".ymaps-2-1-79-map").remove();
    initializeBooking(stationIdsList);
    $("#map").css({
        "margin-top": "5.5px",
        "height": "523px"
    });
    $(".ymaps-2-1-79-islets_icon-with-caption").css({
        "display": "none"
    });
    $("#booking").show();
    $(".ymaps-2-1-79-map").css({
        "height": "518px"
    });
}

function setWindows(){
    let dateSplit = $("#trip_date").val().split("-");
    let chosenDate = new Date();
    chosenDate.setDate(dateSplit[2]);
    chosenDate.setMonth(parseInt(dateSplit[1]) - 1);
    chosenDate.setFullYear(dateSplit[0]);

    if (new Date().getTime() === chosenDate.getTime()) {
        let minutes = chosenDate.getMinutes();
        if (minutes <= 20) {
            chosenDate.setMinutes(20);
        } else {
            if (minutes <= 40) {
                chosenDate.setMinutes(40);
            } else {
                chosenDate.setHours(chosenDate.getHours() + 1);
                chosenDate.setMinutes(0);
            }
        }
        chosenDate.setSeconds(0);
    } else {
        chosenDate.setHours(0);
        chosenDate.setMinutes(0);
        chosenDate.setSeconds(0);
    }

    let dateStringSplit = getDateString(chosenDate).split("-");
    let dateString = dateStringSplit[2] + "." +
        dateStringSplit[1] + "." + dateStringSplit[0];
    let chosenDay = chosenDate.getDate();

    while (chosenDate.getHours() !== 0 || chosenDate.getDate() === chosenDay) {
        let windowString = dateString + " ";
        windowString = formHourMinuteFormat(windowString, chosenDate);
        windowString += "-";
        chosenDate.setMinutes(chosenDate.getMinutes() + 20);

        windowString = formHourMinuteFormat(windowString, chosenDate);
        windowsList.push(windowString);
    }
}

function filterWindows(){
    $.each(scheduleList, function (index, schedule) {
        let bookedTimeWindowsList = [];
        let key = schedule.stationId.toString();
       $.each(schedule.timeWindowsList, function (index, timeWindow){
           bookedTimeWindowsList.push(timeWindow);
       });

       let availWindowsListForId = [];
       if (bookedTimeWindowsList.length === 0) {
           availWindowsListForId = windowsList;
       } else {
           $.each(windowsList, function (index, timeWindow) {
               if (bookedTimeWindowsList.indexOf(timeWindow) === -1) {
                   availWindowsListForId.push(timeWindow);
               }
           });
       }

       availWindowsListForId = availWindowsListForId.sort();
       let availWindows =
           {
               "stationId": key,
               "timeWindowsList": availWindowsListForId
           }
       availWindowsList.push(availWindows);
    });

    drawWindows();
}

function drawWindows() {
    $.each(availWindowsList, function (index, obj) {
        $.each(obj.timeWindowsList, function (index2, timeWindow) {
            let afterIndex = "_" + String(obj.stationId.toString()) +
                "_" + String(index2);
            indexWindowsList.push(afterIndex);
            let idName = "window" + afterIndex;

            $(".booking__period").append('<input type="checkbox" id=' + '"' + idName +
                '" ' + 'value=' + '" ' + timeWindow + '"' + '/>');
            $(".booking__period").append('<label class="booking__period-' +
                idName + '"' + ' for=' + '"'
                + idName + '"> ' + "&nbsp;" + timeWindow.split(" ")[0] +
                "<br>" + timeWindow.split(" ")[1] + "</label>");

        });
    });

    slideWindows();
    loadFullReachTime();
}

function slideWindows(){
    let currentId = id.val().toString();
    $.each(indexWindowsList, function(index, value){
        if (value.split("_")[1] === currentId){
            $(CELL_SLIDE + value).css({
                "display": "block"
            });
        }else{
            $(CELL_SLIDE + value).css({
                "display": "none"
            })
        }
    });

    $.each(routeResponse, function (index, node) {
        if (node.routeNode.id === parseInt(currentId)) {
            let reachTimeIns = REACH_TIME_DESC + getReachTime(node);
            $(".booking__reach-desc").text(reachTimeIns);
            let chargeTimeIns = CHARGE_TIME_DESC + getChargeTime(node);
            $(".booking__charge-desc").text(chargeTimeIns);

            return false;
        }
    });
}

function loadFullReachTime() {
    let routeNodeLast = routeResponse[routeResponse.length-1];
    let fullTimeIns = FULL_TIME_DESC + getReachTime(routeNodeLast);

    $(".booking__fullreach-desc").text(fullTimeIns);
}

function getReachTime(routeNode) {
    let time = " ";
    time += routeNode.reachDuration.hours.toString();
    time += "ч. "
    time += routeNode.reachDuration.minutes.toString();
    time += "м."

    return time;
}

function getChargeTime(routeNode) {
    let time = " ";
    time += routeNode.chargeDuration.hours.toString();
    time += "ч. "
    time += routeNode.chargeDuration.minutes.toString();
    time += "м."

    return time;
}

function displayBookingPanel(){
    setWindows();
    getTimeWindows();
}

function extend(){
    let digits = id.val().toString().length;
    let width =  Number(digits)*10;
    id.css({
        "width": width
    });
}

function getCode() {
    let randomLetters = _ => String.fromCharCode(0|Math.random()*26+97),
        randomChars = Array(4).fill().map(randomLetters).join("");
    let randomNumbers  = Math.round(Math.random()*10000).toString();

    return randomChars + randomNumbers;
}

function formHourMinuteFormat(windowString, chosenDate) {
    windowString += addTrailingZero(chosenDate.getHours());
    windowString += ":";
    windowString += addTrailingZero(chosenDate.getMinutes());

    return windowString;
}

function addTrailingZero(num) {
    if (num < 10) {
        return "0" + num.toString();
    } else {
        return num.toString();
    }
}
