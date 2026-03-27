import json
from dotenv import load_dotenv
import serpapi
import os

def fetch_events(client, query, location):
    results = client.search({
        "engine": "google",
        "q": query,
        "location": location,
        "hl": "en",
        "gl": "us"
    })

    return results.get("events_results", [])

def main():
    load_dotenv()

    API_KEY = os.getenv("SERPAPI_KEY")
    client = serpapi.Client(api_key=API_KEY)

    with open("./locations.txt") as f:
        locations = [line.strip() for line in f if line.strip()]

    with open("./queries.txt") as f:
        queries = [line.strip() for line in f if line.strip()]

    all_events = []

    for loc in locations:
        for query in queries:
            print(f"Fetching {query} for {loc}...")
            events = fetch_events(client, query, loc)

            for e in events:
                if e not in all_events:
                    all_events.append(e)

    with open("events.json", "w") as f:
        json.dump(all_events, f, indent=4)

    print(f"\nSaved {len(all_events)} total events.")

if __name__ == "__main__":
    main()
