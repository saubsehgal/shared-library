package com.comp.jenkins
import com.cloudbees.groovy.cps.NonCPS

class Util {

    @NonCPS
    static ifVariableIsNullReturnThisDefaultValue(variable, defaultValue) {
        return variable ?: defaultValue
    }

}
