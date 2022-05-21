# Common extractor configuration
The following configuration keys apply to all extractors:

## Extraction mode
Sometimes in your process you will need the output of more extractors to build the final data
you need to consume. For example, you might need to join data present in two databases, or in
one database and in an http service.

By default, an extractors overwrites the data of the previous step with its own data, but it is
possible to override this behaviour by providing the configuration key `mode` and setting it to
`merge`:

```json
{
  "type": "ExtractorClassName",
  "config": {
    "mode": "merge"
  }
}
```

In this way, the data coming from the previous step is preserved and added to the next extraction.
For example, let's pretend that extractor no.1 provides as data the following object:

```json
{
  "city": "Venice",
  "population": 261905
}
```

and extractor no.2 extracts the following:

```json
{
  "mostVisited": [
    "Piazza San Marco",
    "Teatro La Fenice"
  ]
}
```

if extractor no.2 is configured in `merge` mode, the output will be the following:

```json
{
  "city": "Venice",
  "population": 261905,
  "mostVisited": [
    "Piazza San Marco",
    "Teatro La Fenice"
  ]
}
```

## Extraction conflicts
It may happen that two or more extractors extract data with the same keys, for example if you're
trying to cross-join two databases' tables you may have the same `id` field.

The default merge behavior, in this case, is to overwrite the old data with the new one, but in
case you need both data somewhere else, you can configure the conflict resolution mode
by specifying the `conflict` configuration object and providing one of the following actions:

- `prepend` will prepend a custom string to all the keys with the same name in the new extraction
- `append` will append a custom string to all the keys with the same name in the new extraction
- `substitute` will drop the old key and replace it with the new one.

You can configure `prepend` and `append` in this way:

### Prepend

Here an example of `prepend`

If extractor no.1 extracts
```json
{
  "id": 1,
  "name": "Mel C"
}
```

and extractor no.2 extracts

```json
{
  "id": 1,
  "name": "Spice Girls"
}
```

we can configure extractor no.2 to prepend `band_` to the newly extracted record like this:

```json
{
  "type": "ExtractorClassName",
  "config": {
    "mode": "merge",
    "conflict": {
      "action": "prepend",
      "value": "band_"
    }
  }
}
```

and obtain the following result:

```json
{
  "id": 1,
  "name": "Mel C",
  "band_id": 1,
  "band_name": "Spice Girls"
}
```

### Append

Here an example of `append`

If extractor no.1 extracts
```json
{
  "id": 1,
  "name": "Mel C"
}
```

and extractor no.2 extracts

```json
{
  "id": 1,
  "name": "Spice Girls"
}
```

we can configure extractor no.2 to append `_band` to the newly extracted record like this:

```json
{
  "type": "ExtractorClassName",
  "config": {
    "mode": "merge",
    "conflict": {
      "action": "append",
      "value": "_band"
    }
  }
}
```

and obtain the following result:

```json
{
  "id": 1,
  "name": "Mel C",
  "id_band": 1,
  "name_band": "Spice Girls"
}
```
