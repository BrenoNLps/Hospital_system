package br.ifsp.hospital.suite;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Suíte de Persistência – Queries customizadas e restrições de banco")
@SelectPackages("br.ifsp.hospital")
@IncludeTags("PersistenceTest")
public class PersistenceTestSuite {
}
