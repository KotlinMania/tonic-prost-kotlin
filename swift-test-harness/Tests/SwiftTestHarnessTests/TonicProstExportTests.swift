import Testing
import TonicProst

@Suite struct TonicProstExportTests {
    @Test func testSwiftModuleLoads() throws {
        #expect(TonicProst.shared.MODULE_NAME == "tonic_prost")
        #expect(TonicProst.shared.VERSION == "0.13.1")
        #expect(Status.Code.OK.value == 0)
    }
}
