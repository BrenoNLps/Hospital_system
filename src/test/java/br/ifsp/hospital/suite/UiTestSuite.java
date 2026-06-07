package br.ifsp.hospital.suite;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Suíte de UI – Testes Selenium com Page Object")
@SelectPackages("br.ifsp.hospital")
@IncludeTags("UiTest")
public class UiTestSuite {
}
