
function reportspluginTest() {
	return "reports-plugin.js loaded";
}
console.log("request-plugin.js loaded");




function baseURL() {

}

/**
* Turn back time in parameter date. (use period)
*
* Set date backawards in time depending on param 'num' and gadget.getPref
* @param {Date}    date        Date to set
* @param {int}     num         Number of time units to set date backwards
* 
* @return {type} void
*/
function setDateBackwardsInTime(date, num, unit) {
    if ( unit == "d" )
        date.setDate(date.getDate() - num);
    else if ( unit == "w" )
        date.setDate(date.getDate() - num*7);
    else if ( unit == "M" )
        date.setMonth(date.getMonth() - num);
    else
        date.setYear(date.getFullYear() - num);
}

/**
* Parse json to google line chart
*
* Issues will be sorted in ascending order 
* Loop until date == createdDate for next issue in json
*
* Set date backawards in time depending on param 'num' and gadget.getPref
* @param {Date}    date        Date to set
* @param {int}     num         Number of time units to set date backwards
* 
* @return {type} void
*/
function parseDataToChart(arg) {
	var total = 0;
	var closed = 0;
	// Loop in acsending dates
	var i = 0;
	while (  i < arg["json"].length ) 
	{	
		var dateString = arg["json"][i].issuedate.toString().split(" ")[0];
		addRow(arg, dateString, total, closed)

	    if ( arg["date"].toISOString().substr(0, 10) >= arg["json"][i].issuedate.toString().split(" ")[0] )
	    {
	        total = parseInt(arg["json"][i].total);
	        closed = parseInt(arg["json"][i].closed);
	    }
	    i++;
	}

	// Check if date have not iterated to todays date
	var now = new Date();
	now.setTime( now.getTime() - new Date().getTimezoneOffset()*60*1000 );
	while ( arg["date"].toISOString().substr(0, 10) <= now.toISOString().substr(0, 10) )
	{
		var dateString = now.toISOString().substr(0, 10);
		addRow(arg, dateString, total, closed);
	    setDateBackwardsInTime(arg["date"], -1, arg["timeUnit"]);
	}
}

/**
* Turn back time in parameter date. (use period)
*
* Issues will be sorted in ascending order 
* Loop until date == createdDate for next issue in json
*
* Set date backawards in time depending on param 'num' and gadget.getPref
* @param {Date}    date        Date to set
* @param {int}     num         Number of time units to set date backwards
* 
* @return {type} void
*/
function addRow(arg, dateString, total, closed) {
	console.log("D2" +arg["json"]);
	var firstTrendPoint = (arg["json"].length > 0) ? (parseInt(arg["json"][0].total) - parseInt(arg["json"][0].closed)) : 0;
	while ( arg["date"].toISOString().substr(0, 10).replace('T', ' ') < dateString ) 
	{ 
		// Ship 6 == saturday and 0 == Sunday Only when day
	    if ( (arg["date"].getUTCDay() == 6 || arg["date"].getUTCDay() == 0) && arg["timeUnit"] == "d") 
	    {
	        arg["date"].setDate(arg["date"].getDate() +1 );
	        continue; 
	    }

	    var insertOpen = total;
	    var insertClosed = closed;
	    if ( arg["countFromZero"] == "true" ) {
	    	if ( arg["json"].length > 0 ) {
	            insertOpen -=  parseInt(arg["json"][0].total);
	            insertClosed -= parseInt(arg["json"][0].closed);
	        }
	    }
	    else {
	        insertOpen -= insertClosed;
	    }

	    //console.log("Add row:[" + arg["date"].toISOString().substr(0, 19).replace('T', ' ') + " Total: " + total + " | Closed: " + insertClosed + " | Open :" + insertOpen + " ]");
	    arg["chartData"].addRow([arg["date"].toISOString().substr(0, 10), total, insertClosed, insertOpen ]);
	    arg["trendData"].addRow([arg["date"].toISOString().substr(0, 10), (total - closed) - firstTrendPoint]);
	    setDateBackwardsInTime(arg["date"], -1, arg["timeUnit"]);
	}
}

