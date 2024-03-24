package org.jenkinsci.plugins.credentialsbinding.impl

class UsernamePasswordMultiBinding {
    String usernameVariable;
    String passwordVariable;
    String credentialsId;


    UsernamePasswordMultiBinding(String usernameVariable, String passwordVariable, String credentialsId) {
        this.usernameVariable = usernameVariable;
        this.passwordVariable = passwordVariable;
        this.credentialsId = credentialsId;
    }
}
