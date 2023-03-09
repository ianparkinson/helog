/* An app for the Hubitat Elevation which periodically writes some log entries at each level, and also sends some
   events of various forms. Useful for testing Helog. */

definition(
        name: "SimpleLogger",
        namespace: "ianparkinson",
        author: "Ian Parkinson",
        description: "Writes some events to the log",
        iconUrl: "",
        iconX2Url: "",
)

preferences {
    section("Config") {
        input name: "enabled", type: "bool", title: "Enabled", defaultValue: false
        input name: "period", type: "number", title: "Period between log writes, in seconds", defaultValue: 60
    }
}

def installed() {
    updated()
}

def updated() {
    log.debug "enabled=${enabled}, period=${period}"
    unschedule("logPeriodically")
    state.count = 0
    if (enabled) {
        runIn(period, "logPeriodically")
    }
}

def uninstalled() {
    log.debug "uninstalled()"
}

def logPeriodically() {
    log.trace "This is a trace-level log message"
    log.debug "This is a debug-level log message"
    log.info "This is an info-level log message"
    log.warn "This is a warn-level log message"
    log.error "This is an error-level log message"

    sendEvent(name: "simpleEvent", value: state.count)
    sendEvent(name: "eventWithUnit", value: state.count, unit: "jiffy")
    sendEvent(name: "eventWithDescription", value: state.count, unit: "jiffy", descriptionText: "This is an event")
    state.count = state.count + 1

    runIn(period, "logPeriodically")
}
