import SwaggerUI from 'swagger-ui-dist/swagger-ui-bundle.js';
import StandalonePreset from 'swagger-ui-dist/swagger-ui-standalone-preset.js';
import 'swagger-ui-dist/swagger-ui.css';

SwaggerUI({
  dom_id: '#swagger-ui',
  layout: 'StandaloneLayout',
  presets: [SwaggerUI.presets.apis, StandalonePreset],
  urls: [
    { name: '01 - Authentication', url: '/docs-api/auth/openapi.json' },
    { name: '02 - User', url: '/docs-api/user/v3/api-docs' },
    { name: '03 - Balance', url: '/docs-api/balance/v3/api-docs' },
    { name: '04 - Tuition', url: '/docs-api/tuition/v3/api-docs' },
    { name: '05 - Payment', url: '/docs-api/payment/v3/api-docs' },
    { name: '06 - OTP (internal)', url: '/docs-api/otp/api-docs' },
    { name: '07 - Email (internal)', url: '/docs-api/email/v3/api-docs' },
  ],
  persistAuthorization: false,
  validatorUrl: null,
  queryConfigEnabled: false,
  tryItOutEnabled: false,
});
