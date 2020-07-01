using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;


namespace StudiumAnalyticsBackend.Models
{
    public class Teaching
    {

        public string userId { get; set; }

        public string dbName { get; set; }

        public string teachingName { get; set; }

        public string tutorName { get; set; }

        public override string ToString()
        {
           return JsonSerializer.Serialize<Teaching>(this);
        }
        
    }
}
