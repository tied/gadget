function restRequestsTest() {
    return "test.js loaded";
}
console.log("restrequests.js loaded");

function getCurrentUserName()
{
	var user;
	AJS.$.ajax({
	url: "/rest/gadget/1.0/currentUser",
	type: 'get',
	dataType: 'json',
	async: false,
	success: function(data) { user = data["username"]; }
	});
	return user;
}

function getFieldInJasonAtURL(targetField)
{
	var result;
	AJS.$.ajax({
	url: "/rest/api/2/search?jql=project='PROJ'",
	type: 'POST',
	dataType: 'json',
	async: false,
	success: function(data) { result = data["targetField"]; }
	});
	return result;
}

/*
var clientId = "admin";
var clientSecret = "admin";

// var authorizationBasic = $.base64.btoa(clientId + ':' + clientSecret);
var authorizationBasic = window.btoa(clientId + ':' + clientSecret);

var request = new XMLHttpRequest();
request.open('POST', oAuth.AuthorizationServer, true);
request.setRequestHeader('Content-Type', 'application/json');
request.setRequestHeader('Authorization', 'Basic ' + authorizationBasic);
request.setRequestHeader('Accept', 'application/json');
request.send("username=admin&password=admin&grant_type=password");

request.onreadystatechange = function () {
    if (request.readyState === 4) {
       alert(request.responseText);
    }
};

var Client = require('node-rest-client').Client;
client = new Client();
// Provide user credentials, which will be used to log in to JIRA.
var loginArgs = {
    data: {
        "username": "admin",
        "password": "admin"
    },
    headers: {
        "Content-Type": "application/json"
    }
};

client.post("http://w405anl:2990/jira/rest/auth/1/session", loginArgs, function(data, response){
    if (response.statusCode == 200) {
        console.log('succesfully logged in, session:', data.session);
        var session = data.session;
        // Get the session information and store it in a cookie in the header
        var searchArgs = {
            headers: {
                // Set the cookie from the session information
                cookie: session.name + '=' + session.value,
                "Content-Type": "application/json"
            },
            data: {
                // Provide additional data for the JIRA search. You can modify the JQL to search for whatever you want.
                jql: "type=Bug AND status=Closed"
            }
        };
        // Make the request return the search results, passing the header information including the cookie.
        client.post("http://localhost:8090/jira/rest/api/2/search", searchArgs, function(searchResult, response) {
            console.log('status code:', response.statusCode);
            console.log('search result:', searchResult);
        });
    } else {
        throw "Login failed :(";
    }
});
*/
/*
var HttpClient = function(gadget) {
    gadget.get = function(aUrl, aCallback) {
        var anHttpRequest = new XMLHttpRequest();
        anHttpRequest.onreadystatechange = function() { 
            if (anHttpRequest.readyState == 4 && anHttpRequest.status == 200)
                aCallback(anHttpRequest.responseText);
        }

        anHttpRequest.open( "GET", aUrl, true );            
        anHttpRequest.send( null );
        console.log("HttpClient")
    }
}
*/
/*
var HttpClient = function() {
    this.get = function(aUrl, aCallback) {
        var anHttpRequest = new XMLHttpRequest();
        anHttpRequest.onreadystatechange = function() { 
            if (anHttpRequest.readyState == 4 && anHttpRequest.status == 200)
                aCallback(anHttpRequest.responseText);
        }

        anHttpRequest.open( "GET", aUrl, true );            
        anHttpRequest.send( null );
        console.log("HttpClient")
    }
}
*/



