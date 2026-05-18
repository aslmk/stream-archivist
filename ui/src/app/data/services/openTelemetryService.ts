import {Injectable} from '@angular/core';
import {defaultResource, resourceFromAttributes} from '@opentelemetry/resources';
import {ATTR_SERVICE_NAME} from '@opentelemetry/semantic-conventions';
import {BatchSpanProcessor, WebTracerProvider} from '@opentelemetry/sdk-trace-web';
import {FetchInstrumentation} from '@opentelemetry/instrumentation-fetch';
import {XMLHttpRequestInstrumentation} from '@opentelemetry/instrumentation-xml-http-request';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import {OTLPTraceExporter} from '@opentelemetry/exporter-trace-otlp-http';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root'
})
export class OpenTelemetryService {

  public initializeOpenTelemetry(): void {
    const resource = defaultResource().merge(
      resourceFromAttributes({
        [ATTR_SERVICE_NAME]: 'frontend',
      })
    );

    const spanProcessors = [];
    spanProcessors.push(new BatchSpanProcessor(new OTLPTraceExporter({
      url: environment.traceEndpoint
    })))

    const provider = new WebTracerProvider({ resource, spanProcessors });
    provider.register();

    registerInstrumentations({
      instrumentations: [
        new FetchInstrumentation({ propagateTraceHeaderCorsUrls: /.*/ }),
        new XMLHttpRequestInstrumentation(),
      ],
    });
  }
}
