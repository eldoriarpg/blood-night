---
title: Finding problems
sidebar_position: 4
---

# Finding problems

Something does not work as planned? These commands help.

## See all problems

```
/bloodnight problems
/bloodnight problems <mob>
```

This lists two kinds of problems:

- **Build problems.** A graph has a mistake, so the mob can't be built. It does not run.
- **Run problems.** Something went wrong while a mob was running. BloodNight tells you once per mob and trigger,
  so your log stays readable.

## Try a mob

```
/bloodnight spawn <mob>
```

Spawns the mob where you stand. From the console, it spawns at the first world's spawn point.

## Fire the debug trigger

```
/bloodnight debug
/bloodnight debug <mob>
```

Every mob with a **Debug Trigger** node in its graph runs it now. Connect it to what you want to test, plus a
**Print** node to see values in the console.

## Look at the code

```
/bloodnight source <mob>
```

Writes the Java code BloodNight made from the graph to `plugins/BloodNight/debug/<mob>.java`. Helpful if you
know some Java, or if you want to report a bug.

## After changing files

```
/bloodnight reload
```

Reads all files again and builds every mob again. It tells you how many mobs it built and how many problems it
found.
