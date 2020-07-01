using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Confluent.Kafka;

using StudiumAnalyticsBackend.Models;

namespace StudiumAnalyticsBackend.Controllers
{
    //iphone address 131 -- casa sergio
    [ApiController]
    [Route("api/[controller]")]
    public class StudiumAnalytics : ControllerBase
    {
        
        [HttpGet]
        public string test(){
            return "ciao";
        }

        [HttpPost("TeachingDidLoad")]
        public async Task<string> teachingDidLoad([FromBody] Teaching teaching)
        {
            Console.WriteLine("TeachingDidLoad: "+ teaching.ToString());
            var result = await produce("TeachingDidLoad", teaching.ToString());
            return result;
        }


        [HttpPost("SectionDidLoad")]
        public async Task<string> sectionDidLoad([FromBody] TeachingSection teachingSection)
        {
            Console.WriteLine("SectionDidLoad: "+teachingSection.ToString());
            var result = await produce("SectionDidLoad", teachingSection.ToString());
            return result;
        }

        [HttpPost("TeachingStateRefresh")]
        public async Task<string> teachingStateRefresh([FromBody] TeachingState teachingState)
        {
            Console.WriteLine("TeachingStateRefresh: " + teachingState.ToString());
            var result = await produce("TeachingStateRefresh", teachingState.ToString());
            return result;
        }

        [HttpPost("DocumentDidOpen")]
        public async Task<string> documentDidOpen([FromBody] DocumentItem item)
        {
            Console.WriteLine("DocumentDidOpen: "+ item.ToString());
            var result = await produce("DocumentDidOpen", item.ToString());
            return result;
        }

        [HttpPost("NotificationDidOpen")]
        public async Task<string> notificationDidOpen([FromBody] NotificationItem item)
        {
            Console.WriteLine("NotificationDidOpen: " + item.ToString());
            var result = await produce("NotificationDidOpen", item.ToString());
            return result;
        }






        private async Task<string> produce(string topic , string messageValue)
        {
            var config = new ProducerConfig { BootstrapServers = "localhost:9092" };
            using (var p = new ProducerBuilder<Null, string>(config).Build())
            {
                try
                {
                    var dr = await p.ProduceAsync(topic, new Message<Null, string> { Value = messageValue });
                    //Console.WriteLine($"Delivered '{dr.Value}' to '{dr.TopicPartitionOffset}'");
                    return "{\"success\": true}";
                }
                catch (ProduceException<Null, string> e)
                {
                    //Console.WriteLine($"Delivery failed: {e.Error.Reason}");
                    return "{\"success\": false}";
                }
            }

        }

    }
}
