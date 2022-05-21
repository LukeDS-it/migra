
# Process file
The anatomy of a process file is very simple, you can take the following base and create your
own:

```json
{
  "extract": [],
  "consume": []
}
```

In the `extract` array you will add all extractors that you need in your process. They will be
execute one after another, and each step will take as additional input data the result of the
previous step. Example:

```json
{
  "type": "ExtractorClassName",
  "config": {
    "config-key": "config-value" 
  }
}
```

In the `consume` array you will add all consumers that you want to output data to. They will be
executed in parallel, and each one will take as input the output of the last extractor in the
process. Example:

```json
{
  "type": "ConsumerClassName",
  "config": {
    "config-key": "config-value" 
  }
}
```

To see the configuration of consumers, see the relative file.
