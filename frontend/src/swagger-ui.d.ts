declare module 'swagger-ui-dist/swagger-ui-bundle.js' {
  interface SwaggerFactory {
    (options: Record<string, unknown>): unknown;
    presets: { apis: unknown };
  }
  const SwaggerUI: SwaggerFactory;
  export default SwaggerUI;
}
declare module 'swagger-ui-dist/swagger-ui-standalone-preset.js' {
  const preset: unknown;
  export default preset;
}
