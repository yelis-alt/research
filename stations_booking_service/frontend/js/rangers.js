function controlfromInput_kvt(fromSlider_kvt, fromInput_kvt, toInput_kvt, controlSlider) {
  const [from, to] = getParsed(fromInput_kvt, toInput_kvt);
  fillSlider(fromInput_kvt, toInput_kvt, '#C6C6C6', '#000000', controlSlider);
  if (from > to) {
      fromSlider_kvt.value = to;
      fromInput_kvt.value = to;
  } else {
      fromSlider_kvt.value = from;
  }
}
  
function controltoInput_kvt(toSlider_kvt, fromInput_kvt, toInput_kvt, controlSlider) {
  const [from, to] = getParsed(fromInput_kvt, toInput_kvt);
  fillSlider(fromInput_kvt, toInput_kvt, '#C6C6C6', '#000000', controlSlider);
  setToggleAccessible(toInput_kvt);
  if (from <= to) {
      toSlider_kvt.value = to;
      toInput_kvt.value = to;
  } else {
      toInput_kvt.value = from;
  }
}

function controlfromSlider_kvt(fromSlider_kvt, toSlider_kvt, fromInput_kvt) {
const [from, to] = getParsed(fromSlider_kvt, toSlider_kvt);
fillSlider(fromSlider_kvt, toSlider_kvt, '#C6C6C6', '#000000', toSlider_kvt);
if (from > to) {
  fromSlider_kvt.value = to;
  fromInput_kvt.value = to;
} else {
  fromInput_kvt.value = from;
}
}

function getParsed(currentFrom, currentTo) {
const from = parseInt(currentFrom.value, 10);
const to = parseInt(currentTo.value, 10);
return [from, to];
}

function controltoSlider_kvt(fromSlider_kvt, toSlider_kvt, toInput_kvt) {
const [from, to] = getParsed(fromSlider_kvt, toSlider_kvt);
fillSlider(fromSlider_kvt, toSlider_kvt, '#C6C6C6', '#000000', toSlider_kvt);
setToggleAccessible(toSlider_kvt);
if (from <= to) {
  toSlider_kvt.value = to;
  toInput_kvt.value = to;
} else {
  toInput_kvt.value = from;
  toSlider_kvt.value = from;
}
}

function fillSlider(from, to, sliderColor, rangeColor, controlSlider) {
  const rangeDistance = to.max-to.min;
  const fromPosition = from.value - to.min;
  const toPosition = to.value - to.min;
  controlSlider.style.background = `linear-gradient(
    to right,
    ${sliderColor} 0%,
    ${sliderColor} ${(fromPosition)/(rangeDistance)*100}%,
    ${rangeColor} ${((fromPosition)/(rangeDistance))*100}%,
    ${rangeColor} ${(toPosition)/(rangeDistance)*100}%, 
    ${sliderColor} ${(toPosition)/(rangeDistance)*100}%, 
    ${sliderColor} 100%)`;
}

function setToggleAccessible(currentTarget) {
const toSlider_kvt = document.querySelector('#toSlider_kvt');
if (Number(currentTarget.value) <= 0 ) {
  toSlider_kvt.style.zIndex = 2;
} else {
  toSlider_kvt.style.zIndex = 0;
}
}

const fromSlider_kvt = document.querySelector('#fromSlider_kvt');
const toSlider_kvt = document.querySelector('#toSlider_kvt');
const fromInput_kvt = document.querySelector('#fromInput_kvt');
const toInput_kvt = document.querySelector('#toInput_kvt');
fillSlider(fromSlider_kvt, toSlider_kvt, '#C6C6C6', '#000000', toSlider_kvt);
setToggleAccessible(toSlider_kvt);

fromSlider_kvt.oninput = () => controlfromSlider_kvt(fromSlider_kvt, toSlider_kvt, fromInput_kvt);
toSlider_kvt.oninput = () => controltoSlider_kvt(fromSlider_kvt, toSlider_kvt, toInput_kvt);
fromInput_kvt.oninput = () => controlfromInput_kvt(fromSlider_kvt, fromInput_kvt, toInput_kvt, toSlider_kvt);
toInput_kvt.oninput = () => controltoInput_kvt(toSlider_kvt, fromInput_kvt, toInput_kvt, toSlider_kvt);

function controlfromInput_price(fromSlider_price, fromInput_price, toInput_price, controlSlider) {
  const [from, to] = getParsed(fromInput_price, toInput_price);
  fillSlider(fromInput_price, toInput_price, '#C6C6C6', '#000000', controlSlider);
  if (from > to) {
      fromSlider_price.value = to;
      fromInput_price.value = to;
  } else {
      fromSlider_price.value = from;
  }
}
  
function controltoInput_price(toSlider_price, fromInput_price, toInput_price, controlSlider) {
  const [from, to] = getParsed(fromInput_price, toInput_price);
  fillSlider(fromInput_price, toInput_price, '#C6C6C6', '#000000', controlSlider);
  setToggleAccessible(toInput_price);
  if (from <= to) {
      toSlider_price.value = to;
      toInput_price.value = to;
  } else {
      toInput_price.value = from;
  }
}

function controlfromSlider_price(fromSlider_price, toSlider_price, fromInput_price) {
const [from, to] = getParsed(fromSlider_price, toSlider_price);
fillSlider(fromSlider_price, toSlider_price, '#C6C6C6', '#000000', toSlider_price);
if (from > to) {
  fromSlider_price.value = to;
  fromInput_price.value = to;
} else {
  fromInput_price.value = from;
}
}

function controltoSlider_price(fromSlider_price, toSlider_price, toInput_price) {
const [from, to] = getParsed(fromSlider_price, toSlider_price);
fillSlider(fromSlider_price, toSlider_price, '#C6C6C6', '#000000', toSlider_price);
setToggleAccessible(toSlider_price);
if (from <= to) {
  toSlider_price.value = to;
  toInput_price.value = to;
} else {
  toInput_price.value = from;
  toSlider_price.value = from;
}
}

function getParsed(currentFrom, currentTo) {
const from = parseInt(currentFrom.value, 10);
const to = parseInt(currentTo.value, 10);
return [from, to];
}

function fillSlider(from, to, sliderColor, rangeColor, controlSlider) {
  const rangeDistance = to.max-to.min;
  const fromPosition = from.value - to.min;
  const toPosition = to.value - to.min;
  controlSlider.style.background = `linear-gradient(
    to right,
    ${sliderColor} 0%,
    ${sliderColor} ${(fromPosition)/(rangeDistance)*100}%,
    ${rangeColor} ${((fromPosition)/(rangeDistance))*100}%,
    ${rangeColor} ${(toPosition)/(rangeDistance)*100}%, 
    ${sliderColor} ${(toPosition)/(rangeDistance)*100}%, 
    ${sliderColor} 100%)`;
}

function setToggleAccessible(currentTarget) {
const toSlider_price = document.querySelector('#toSlider_price');
if (Number(currentTarget.value) <= 0 ) {
  toSlider_price.style.zIndex = 2;
} else {
  toSlider_price.style.zIndex = 0;
}
}

const fromSlider_price = document.querySelector('#fromSlider_price');
const toSlider_price = document.querySelector('#toSlider_price');
const fromInput_price = document.querySelector('#fromInput_price');
const toInput_price = document.querySelector('#toInput_price');
fillSlider(fromSlider_price, toSlider_price, '#C6C6C6', '#000000', toSlider_price);
setToggleAccessible(toSlider_price);

fromSlider_price.oninput = () => controlfromSlider_price(fromSlider_price, toSlider_price, fromInput_price);
toSlider_price.oninput = () => controltoSlider_price(fromSlider_price, toSlider_price, toInput_price);
fromInput_price.oninput = () => controlfromInput_price(fromSlider_price, fromInput_price, toInput_price, toSlider_price);
toInput_price.oninput = () => controltoInput_price(toSlider_price, fromInput_price, toInput_price, toSlider_price);