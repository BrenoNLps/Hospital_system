package br.ifsp.hospital.suite;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Suíte de Integração – API + Persistência")
@SelectPackages("br.ifsp.hospital")
@IncludeTags("IntegrationTest")
public class IntegrationTestSuite {
}
