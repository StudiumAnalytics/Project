using System;
using System.Text.Json;

namespace StudiumAnalyticsBackend.Models
{
    public class TeachingSection
    {
        public Teaching teaching { get; set; }

        public string sectionName { get; set; }

        public override string ToString()
        {
            return JsonSerializer.Serialize<TeachingSection>(this);
        }
    }
}
