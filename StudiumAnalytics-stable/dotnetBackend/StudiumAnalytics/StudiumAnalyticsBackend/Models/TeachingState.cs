using System;
using System.Text.Json;

namespace StudiumAnalyticsBackend.Models
{
    public class TeachingState
    {
        public Teaching teaching { get; set; }

        public bool showCaseExists { get; set; }

        public bool notifiesExists { get; set; }

        public bool descriptionExists { get; set; }

        public bool documentsExists { get; set; }

        public bool bookingExists { get; set; }

        

        public override string ToString()
        {
            return JsonSerializer.Serialize<TeachingState>(this);
        }
    
    }
}
