using System;
using System.Text.Json;

namespace StudiumAnalyticsBackend.Models
{
    public class Item
    {
        public Teaching teaching { get; set; }

        public string date { get; set; }

        public string title { get; set; }

        public override string ToString()
        {
            return JsonSerializer.Serialize<Item>(this);
        }
    }
}
