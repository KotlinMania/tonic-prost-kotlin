import Testing
import TonicProst

@Suite struct TonicProstExportTests {
    @Test("Swift module loads cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true))
    }
}

