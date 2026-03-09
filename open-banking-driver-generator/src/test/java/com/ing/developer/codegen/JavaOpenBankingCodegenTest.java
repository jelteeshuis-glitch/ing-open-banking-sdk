package com.ing.developer.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/***
 * Tests for the JavaOpenBankingCodegen custom generator.
 */
class JavaOpenBankingCodegenTest {

    // --- Basic generator metadata ---

    @Test
    void getNameShouldReturnJava() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        assertEquals("Java", codegen.getName());
    }

    @Test
    void getHelpShouldReturnDescription() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        String help = codegen.getHelp();
        assertNotNull(help);
        assertTrue(help.contains("ING"), "Help text should mention ING");
    }

    // --- preprocessOpenAPI tests ---

    @Test
    void preprocessShouldRemoveSignatureParameter() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Signature", "Digest", "Date", "Authorization", "X-JWS-Signature", "Accept");

        codegen.preprocessOpenAPI(openAPI);

        List<String> remainingParams = getParamNames(openAPI);
        assertFalse(remainingParams.contains("Signature"), "Signature param should be removed");
    }

    @Test
    void preprocessShouldRemoveDigestParameter() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Signature", "Digest", "Date", "Accept");

        codegen.preprocessOpenAPI(openAPI);

        List<String> remainingParams = getParamNames(openAPI);
        assertFalse(remainingParams.contains("Digest"), "Digest param should be removed");
    }

    @Test
    void preprocessShouldRemoveDateParameter() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Signature", "Digest", "Date", "Accept");

        codegen.preprocessOpenAPI(openAPI);

        List<String> remainingParams = getParamNames(openAPI);
        assertFalse(remainingParams.contains("Date"), "Date param should be removed");
    }

    @Test
    void preprocessShouldKeepNonExcludedParameters() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Signature", "Digest", "Date", "Accept", "Content-Type");

        codegen.preprocessOpenAPI(openAPI);

        List<String> remainingParams = getParamNames(openAPI);
        assertTrue(remainingParams.contains("Accept"), "Accept param should be kept");
        assertTrue(remainingParams.contains("Content-Type"), "Content-Type param should be kept");
    }

    @Test
    void preprocessShouldMakeAuthorizationOptional() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Authorization", "Accept");

        // Make Authorization required initially
        openAPI.getPaths().get("/test").getGet().getParameters()
                .stream().filter(p -> "Authorization".equals(p.getName()))
                .forEach(p -> p.setRequired(true));

        codegen.preprocessOpenAPI(openAPI);

        Parameter authParam = openAPI.getPaths().get("/test").getGet().getParameters()
                .stream().filter(p -> "Authorization".equals(p.getName()))
                .findFirst().orElse(null);

        assertNotNull(authParam, "Authorization param should still exist");
        assertFalse(authParam.getRequired(), "Authorization should be made optional");
    }

    @Test
    void preprocessShouldMakeXJWSSignatureOptional() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("X-JWS-Signature", "Accept");

        openAPI.getPaths().get("/test").getGet().getParameters()
                .stream().filter(p -> "X-JWS-Signature".equals(p.getName()))
                .forEach(p -> p.setRequired(true));

        codegen.preprocessOpenAPI(openAPI);

        Parameter jwsParam = openAPI.getPaths().get("/test").getGet().getParameters()
                .stream().filter(p -> "X-JWS-Signature".equals(p.getName()))
                .findFirst().orElse(null);

        assertNotNull(jwsParam, "X-JWS-Signature param should still exist");
        assertFalse(jwsParam.getRequired(), "X-JWS-Signature should be made optional");
    }

    @Test
    void preprocessShouldHandleNullOpenAPI() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        // Should not throw
        assertDoesNotThrow(() -> codegen.preprocessOpenAPI(null));
    }

    @Test
    void preprocessShouldHandleNullPaths() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(null);
        assertDoesNotThrow(() -> codegen.preprocessOpenAPI(openAPI));
    }

    @Test
    void preprocessShouldHandleOperationWithNullParameters() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setParameters(null);
        pathItem.setGet(operation);
        paths.addPathItem("/test", pathItem);
        openAPI.setPaths(paths);

        assertDoesNotThrow(() -> codegen.preprocessOpenAPI(openAPI));
    }

    @Test
    void preprocessShouldRemoveAllThreeExcludedParamsAtOnce() {
        JavaOpenBankingCodegen codegen = new JavaOpenBankingCodegen();
        OpenAPI openAPI = createOpenAPIWithParams("Signature", "Digest", "Date", "Accept", "Authorization");

        codegen.preprocessOpenAPI(openAPI);

        List<String> remainingParams = getParamNames(openAPI);
        assertEquals(2, remainingParams.size(), "Should have exactly 2 remaining params");
        assertTrue(remainingParams.contains("Accept"));
        assertTrue(remainingParams.contains("Authorization"));
    }

    // --- Code generation test ---

    @Test
    void launchCodeGenerator() {
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("Java")
                .setInputSpec("src/test/resources/petstore.json")
                .addAdditionalProperty("dateLibrary", "java8")
                .addAdditionalProperty("library", "jersey3")
                .setOutputDir("out/open-banking-codegen");

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(clientOptInput).generate();
    }

    // --- Helper methods ---

    private OpenAPI createOpenAPIWithParams(String... paramNames) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();

        List<Parameter> params = new ArrayList<>();
        for (String name : paramNames) {
            Parameter param = new Parameter();
            param.setName(name);
            param.setIn("header");
            params.add(param);
        }
        operation.setParameters(params);
        pathItem.setGet(operation);
        paths.addPathItem("/test", pathItem);
        openAPI.setPaths(paths);
        return openAPI;
    }

    private List<String> getParamNames(OpenAPI openAPI) {
        return openAPI.getPaths().get("/test").getGet().getParameters()
                .stream().map(Parameter::getName).collect(Collectors.toList());
    }
}
