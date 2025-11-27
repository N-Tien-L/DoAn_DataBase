/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlythuvien.exceptions;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author DMX MSI
 */
public class BanDocException extends Exception  {
    private final Map<String, String> violations;

    public BanDocException(Map<String, String> violations) {
        super("BanDoc constraint violation");
        this.violations = violations == null ? Collections.emptyMap() : violations;
    }

    public BanDocException(String message) {
        super(message);
        this.violations = Collections.singletonMap("general", message);
    }

    public Map<String, String> getViolations() {
        return violations;
    }
}
