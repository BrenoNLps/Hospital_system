package br.ifsp.hospital.suite;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Suíte de API – Testes de integração via REST Assured")
@SelectPackages("br.ifsp.hospital")
@IncludeTags("ApiTest")
public class ApiTestSuite {
}
