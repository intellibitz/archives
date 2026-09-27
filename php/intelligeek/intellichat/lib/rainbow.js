/************************************************************************/
/* Rainbow Link Version 1.00 (1999.6.10)                                */
/*                                                                      */
/* Copyright (C) 1999-2001 TAKANASHI Mizuki                             */
/* takanasi@hamal.freemail.ne.jp                                        */
/*----------------------------------------------------------------------*/
/* Read it somehow even if my English text is a little wrong! ;-)       */
/*                                                                      */
/* Usage:                                                               */
/*  Insert '<script src="rainbow.js"></script>' into the BODY section,  */
/*  right after the BODY tag itself, before anything else.              */
/*  You don't need to add "onMouseover" and "onMouseout" attributes!!   */
/*                                                                      */
/*  If you'd like to add effect to other texts(not link texts), then    */
/*  add 'onmouseover="doRainbow();"' and 'onmouseout="stopRainbow();"'  */
/*  to the target tags.                                                 */
/*                                                                      */
/* This Script works with IE4 and above only, but no error occurs on    */
/* other browsers.                                                      */
/************************************************************************/


////////////////////////////////////////////////////////////////////
// Setting

var rate = 20;  // Increase amount(The degree of the transmutation)


////////////////////////////////////////////////////////////////////
// Main routine

var obj;        // The object which event occured in
var act = 0;    // Flag during the action
var elmH = 0;   // Hue
var elmS = 128; // Saturation
var elmV = 255; // Value
var clrOrg;     // A color before the change
var TimerID;    // Timer ID


if (navigator.appName.indexOf("Microsoft",0) != -1 && parseInt(navigator.appVersion) >= 4) {
    Browser = true;
} else {
    Browser = false;
}

if (Browser) {
    document.onmouseover = doRainbowAnchor;
    document.onmouseout = stopRainbowAnchor;
}


//=============================================================================
// doRainbow
//  This function begins to change a color.
//=============================================================================
function doRainbow()
{
    if (Browser && act != 1) {
        act = 1;
        obj = event.srcElement;
        clrOrg = obj.style.color;
        TimerID = setInterval("ChangeColor()",100);
    }
}


//=============================================================================
// stopRainbow
//  This function stops to change a color.
//=============================================================================
function stopRainbow()
{
    if (Browser && act != 0) {
        obj.style.color = clrOrg;
        clearInterval(TimerID);
        act = 0;
    }
}


//=============================================================================
// doRainbowAnchor
//  This function begins to change a color. (of a anchor, automatically)
//=============================================================================
function doRainbowAnchor()
{
    if (Browser && act != 1) {
        obj = event.srcElement;

        while (obj.tagName != 'A' && obj.tagName != 'BODY') {
            obj = obj.parentElement;
            if (obj.tagName == 'A' || obj.tagName == 'BODY')
                break;
        }

        if (obj.tagName == 'A' && obj.href != '') {
            act = 1;
            clrOrg = obj.style.color;
            TimerID = setInterval("ChangeColor()",100);
        }
    }
}


//=============================================================================
// stopRainbowAnchor
//  This function stops to change a color. (of a anchor, automatically)
//=============================================================================
function stopRainbowAnchor()
{
    if (Browser && act != 0) {
        if (obj.tagName == 'A') {
            obj.style.color = clrOrg;
            clearInterval(TimerID);
            act = 0;
        }
    }
}


//=============================================================================
// Change Color
//  This function changes a color actually.
//=============================================================================
function ChangeColor()
{
    obj.style.color = makeColor();
}


//=============================================================================
// makeColor
//  This function makes rainbow colors.
//=============================================================================
function makeColor()
{
    // Don't you think Color Gamut to look like Rainbow?

    // HSVtoRGB
    if (elmS == 0) {
        elmR = elmV;    elmG = elmV;    elmB = elmV;
    }
    else {
        t1 = elmV;
        t2 = (255 - elmS) * elmV / 255;
        t3 = elmH % 60;
        t3 = (t1 - t2) * t3 / 60;

        if (elmH < 60) {
            elmR = t1;  elmB = t2;  elmG = t2 + t3;
        }
        else if (elmH < 120) {
            elmG = t1;  elmB = t2;  elmR = t1 - t3;
        }
        else if (elmH < 180) {
            elmG = t1;  elmR = t2;  elmB = t2 + t3;
        }
        else if (elmH < 240) {
            elmB = t1;  elmR = t2;  elmG = t1 - t3;
        }
        else if (elmH < 300) {
            elmB = t1;  elmG = t2;  elmR = t2 + t3;
        }
        else if (elmH < 360) {
            elmR = t1;  elmG = t2;  elmB = t1 - t3;
        }
        else {
            elmR = 0;   elmG = 0;   elmB = 0;
        }
    }

    elmR = Math.floor(elmR);
    elmG = Math.floor(elmG);
    elmB = Math.floor(elmB);

    clrRGB = '#' + elmR.toString(16) + elmG.toString(16) + elmB.toString(16);

    elmH = elmH + rate;
    if (elmH >= 360)
        elmH = 0;

    return clrRGB;
}
