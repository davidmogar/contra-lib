import org.centos.contra.pipeline.Utils

/**
 * requires: env.topicPrefix
 * optional: env.msgProperties: This optional variable can be defined to pass along custom message headers in the messages sent by sendPipelineStatusMsg
 * Example Usage:
 *
 * sendPipelineStatusMsg('complete') {
 * }
 *
 * @param metricsMap: A map containing the metrics to be sent over the bus
 * @return
 */

def call(Map metricsMap) {
    // Make sure required env variables are set. The ones used in the
    // message bodies are enforced by the json closures.
    // Note: Error message should be changed if variables are added here
    if (!env.topicPrefix) {
        error("Missing env.topicPrefix required variable to use sendMetricMsg")
    }

    def msgTopic = env.topicPrefix + ".pipeline.metrics"

    try {
        service = msgBusMetricServiceContent(
                metricsMap['service']
        )
        error = metricsMap['error'] ? msgBusMetricErrorContent(code: metricsMap['error']['code'], message: metricsMap['error']['message']) : null

        def retryData
        if (metricsMsg['retryData']['configuration']) {
            retryDataConfiguration = msgBusMetricRetryDataConfigurationContent(
                    metricsMap['retryData']['configuration']
            )
            retryData = msgBusMetricRetryDataContent(configuration: retryDataConfiguration, iterations: metricsMap['retryData']['iterations'])
        } else {
            retryData = msgBusMetricRetryDataContent(iterations: metricsMap['retryData']['iterations'])
        }

        externalCall = metricsMap['error'] ? msgBusMetricExternalCallContent(
                service: service,
                source: metricsMap['source'],
                success: metricsMap['success'],
                error: error,
                start: metricsMap['start'],
                end: metricsMap['end'],
                retryData: retryData
        ) : msgBusMetricExternalCallContent(
                service: service,
                source: metricsMap['source'],
                success: metricsMap['success'],
                start: metricsMap['start'],
                end: metricsMap['end'],
                retryData: retryData
        )
        pipeline = msgBusMetricPipelineContent(id: env.pipelineId, name: env.pipelineName, jenkinsURL: env.JENKINS_URL, productID: env.productId)
        metricMsg = msgBusMetricMsg(externalCall: externalCall, generated_at: java.time.Instant.now().toString(), pipeline: pipeline)

        // Send message
        utils.sendMessage(msgTopic: msgTopic, msgProps: env.msgProperties ?: "", msgContent: metricMsg())

    } catch(e) {
        println("No message was sent out on topic " + msgTopic + ". The error encountered was: " + e)
    }
}
