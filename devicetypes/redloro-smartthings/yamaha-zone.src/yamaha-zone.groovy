/**
 *  SmartThings Device Handler: Yamaha Zone
 *
 *  Author: redloro@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
  definition (name: "Yamaha Zone", namespace: "redloro-smartthings", author: "redloro@gmail.com") {
    
    /**
     * List our capabilties. Doing so adds predefined command(s) which
     * belong to the capability.
     */
    capability "Music Player"
    capability "Switch"
    capability "Refresh"
    capability "Polling"
        
    /**
     * Define all commands, ie, if you have a custom action not
     * covered by a capability, you NEED to define it here or
     * the call will not be made.
     *
     * To call a capability function, just prefix it with the name
     * of the capability, for example, refresh would be "refresh.refresh"
     */
    command "mutedOn"
    command "mutedOff"
    command "partyModeOn"
    command "partyModeOff"
    command "zone"
  }

  /**
   * Define the various tiles and the states that they can be in.
   * The 2nd parameter defines an event which the tile listens to,
   * if received, it tries to map it to a state.
   *
   * You can also use ${currentValue} for the value of the event
   * or ${name} for the name of the event. Just make SURE to use
   * single quotes, otherwise it will only be interpreted at time of
   * launch, instead of every time the event triggers.
   */
  tiles(scale: 2) {
    multiAttributeTile(name:"state", type:"generic", width:6, height:4) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'On', action:"switch.off", icon:"st.Electronics.electronics16", backgroundColor:"#79b821", nextState:"off"
        attributeState "off", label:'Off', action:"switch.on", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff", nextState:"on"
      }
      tileAttribute ("source", key: "SECONDARY_CONTROL") {
        attributeState "source", label:'${currentValue}'
      }
    }

    // row
    controlTile("volume", "device.volume", "slider", height: 2, width: 6, range:"(0..100)") {
      state "volume", label: "Volume", action:"music Player.setLevel", backgroundColor:"#ffffff"
    }

    // row
    standardTile("muted", "device.muted", decoration: "flat", width: 2, height: 2) {
      state("off", label:'Muted', action:"mutedOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff", nextState:"on")
      state("on", label:'Muted', action:"mutedOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff", nextState:"off")
    }
    standardTile("partyMode", "device.partyMode", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
      state("off", label:'Party Mode', action:"partyModeOn", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-gray.png", backgroundColor:"#ffffff", nextState:"on")
      state("on", label:'Party Mode', action:"partyModeOff", icon:"https://raw.githubusercontent.com/redloro/smartthings/master/images/indicator-dot-green.png", backgroundColor:"#ffffff", nextState:"off")
    }
    standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
    }

    // Defines which tile to show in the overview    
    main "state"

    // Defines which tile(s) to show when user opens the detailed view
    details([
      "state",
      "volume",
      "muted", "partyMode","refresh"
    ])
  }
}

/**************************************************************************
 * The following section simply maps the actions as defined in
 * the metadata into onAction() calls.
 *
 * This is preferred since some actions can be dealt with more
 * efficiently this way. Also keeps all user interaction code in
 * one place.
 *
 */
def on() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${getZone()}><Power_Control><Power>On</Power></Power_Control></${getZone()}></YAMAHA_AV>")
  sendEvent(name: "switch", value: "on")
}
def off() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${getZone()}><Power_Control><Power>Standby</Power></Power_Control></${getZone()}></YAMAHA_AV>")
  sendEvent(name: "switch", value: "off")
}
def setLevel(value) {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${getZone()}><Volume><Lvl><Val>${(value * 9 - 800).intValue()}</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume></${getZone()}></YAMAHA_AV>")
  sendEvent(name: "volume", value: value)
}
def mutedOn() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${getZone()}><Volume><Mute>On</Mute></Volume></${getZone()}></YAMAHA_AV>")
  sendEvent(name: "muted", value: "on")
}
def mutedOff() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><${getZone()}><Volume><Mute>Off</Mute></Volume></${getZone()}></YAMAHA_AV>")
  sendEvent(name: "muted", value: "off")
}
def partyModeOn() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><System><Party_Mode><Mode>On</Mode></Party_Mode></System></YAMAHA_AV>")
  sendEvent(name: "partyMode", value: "on")
}
def partyModeOff() {
  sendCommand("<YAMAHA_AV cmd=\"PUT\"><System><Party_Mode><Mode>Off</Mode></Party_Mode></System></YAMAHA_AV>")
  sendEvent(name: "partyMode", value: "off")
}
def refresh() {
  sendCommand("<YAMAHA_AV cmd=\"GET\"><${getZone()}><Basic_Status>GetParam</Basic_Status></${getZone()}></YAMAHA_AV>")
  sendCommand("<YAMAHA_AV cmd=\"GET\"><System><Party_Mode><Mode>GetParam</Mode></Party_Mode></System></YAMAHA_AV>")
}
/**************************************************************************/

/**
 * Called every so often (every 5 minutes actually) to refresh the
 * tiles so the user gets the correct information.
 */
def poll() {
  refresh()
}

def parse(String description) {
  return
}

def zone(evt) {
  /*
  * Zone On/Off
  */
  if (evt.Basic_Status.Power_Control.Power.text()) {
    sendEvent(name: "switch", value: (evt.Basic_Status.Power_Control.Power.text() == "On") ? "on" : "off")
  }
  
  /*
  * Zone Volume
  */
  if (evt.Basic_Status.Volume.Lvl.Val.text()) {
    def int volLevel = evt.Basic_Status.Volume.Lvl.Val.toInteger() ?: -250        
    sendEvent(name: "volume", value: ((volLevel + 800) / 9).intValue())
  }
  
  /*
  * Zone Muted
  */
  if (evt.Basic_Status.Volume.Mute.text()) {
    sendEvent(name: "muted", value: (evt.Basic_Status.Volume.Mute.text() == "On") ? "on" : "off")
  }
  
  /*
  * Zone Source
  */
  if (evt.Basic_Status.Input.Input_Sel.text()) {
    sendEvent(name: "source", value: "Source: ${evt.Basic_Status.Input.Input_Sel.text()}")
  }
  
  /*
  * Party Mode
  */
  if (evt.Party_Mode.Mode.text()) {
    sendEvent(name: "partyMode", value: (evt.Party_Mode.Mode.text() == "On") ? "on" : "off")
  }
}

private sendCommand(body) {
  parent.sendCommand(body)
}

private getZone() {
  return new String(device.deviceNetworkId).tokenize('|')[1]
}