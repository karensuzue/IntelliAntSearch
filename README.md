# IntelliAntSearch

---

## NOTE: For Windows, please use git bash to run the make commands.

## Building / Compiling

#### Mac

```bash
$ make
```

#### Windows

```bash
$ make build-windows
```

## Running / Testing

#### Mac

```bash
$ make run config={relative_path_to_config}
```

#### Windows

```bash
$ make run-windows config={relative_path_to_config}
```

---

#### Config 

```bash
$ peersim-1.0.5/config/*.cfg
```

## 

For Flooding, 

```bash
$ peersim-1.0.5/config/flooding.cfg
```

For RW, 

```bash
$ peersim-1.0.5/config/rw.cfg
```

For AntP2PR, 

```bash
$ peersim-1.0.5/config/antp2pr.cfg
```

For DLAnt, 

```bash
$ peersim-1.0.5/config/dlantp2p.cfg
```

NOTE: To run the simulation, make sure you are in the `peersim-1.0.5` directory.

---

#### LSTM -

For the machine learning part, we ran the simulation as normal and copied the results of it to  ```LSTM.ipynb``` and ran the model on them to produce the parameters

#### Protocol Implementations -

All the protocol implementations are in ```peersim-1.0.5/src/iat/**```