
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
		addRow(arg["json"], arg["date"], dateString, arg["timeUnit"], arg["countFromZero"], arg["chartData"], arg["trendData"], total, closed)

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
		addRow(arg["json"], arg["date"], dateString, arg["timeUnit"], arg["countFromZero"], arg["chartData"], arg["trendData"], total, closed);
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
function addRow(json, date, dateString, timeUnit, countFromZero, chartData, trendData, total, closed) {
	var firstTrendPoint = (json.length > 0) ? (parseInt(json[0].total) - parseInt(json[0].closed)) : 0;
	while ( date.toISOString().substr(0, 10).replace('T', ' ') < dateString ) 
	{ 
		// Ship 6 == saturday and 0 == Sunday Only when day
	    if ( (date.getUTCDay() == 6 || date.getUTCDay() == 0) && timeUnit == "d") 
	    {
	        date.setDate(date.getDate() +1 );
	        continue; 
	    }

	    var insertOpen = total;
	    var insertClosed = closed;
	    if ( countFromZero == "true" ) {
	    	if ( json["length"] > 0 ) {
	            insertOpen -=  parseInt(json[0].total);
	            insertClosed -= parseInt(json[0].closed);
	        }
	    }
	    else {
	        insertOpen -= insertClosed;
	    }

	    console.log("Add row:[" + date.toISOString().substr(0, 19).replace('T', ' ') + " Total: " + total + " | Closed: " + insertClosed + " | Open :" + insertOpen + " ]");
	    chartData.addRow([date.toISOString().substr(0, 10), total, insertClosed, insertOpen ]);
	    trendData.addRow([date.toISOString().substr(0, 10), (total - closed) - firstTrendPoint]);
	    setDateBackwardsInTime(date, -1, timeUnit);
	}
}

