/**
 *  Local HTTP GET
 *
 *  Author: Alex Iordanescu
 *
 *  Date: 2014-04-14
 */

import groovy.json.JsonSlurper

preferences {        
        input("ip", "string", title:"IP Address", description: "192.168.1.10", required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "80", defaultValue: "80", required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", description: "", required: false, displayDuringSetup: true)
        input("password", "password", title:"Password", description: "", required: false, displayDuringSetup: true)
        input("uri", "string", title:"URI", description: "URI", required: true, displayDuringSetup: true)
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Local HTTP GET", namespace: "alex", author: "Alex Iordanescu") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
}

def push() {
	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
        postAction(uri)
}

def on() {
	push()
}

def off() {
	push()
}

// ------------------------------------------------------------------

private postAction(uri){
  setDeviceNetworkId(ip,port)  
  
  def userpass = encodeCredentials(username, password)

  def headers = getHeader(userpass)
    
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )  
  log.debug("Executing hubAction on " + getHostAddress())
  hubAction
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
    log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    headers.put("Authorization", userpass)
    //log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex:$porthex"
  	log.debug "Device Network Id set to ${iphex}:${porthex}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}