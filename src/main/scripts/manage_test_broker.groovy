String[] command

def nodename = properties['nodename']
if (nodename == null || nodename.length() == 0) {
  fail("Node name required")
}

switch (mojo.getExecutionId()) {
case ~/^start-test-broker-.*/:
  def node_port = properties['node_port']
  if (node_port == null || node_port.length() == 0) {
    fail("Node TCP port required")
  }

  command = [
    properties['make.bin'],
    '-C', "${properties['deps.dir']}/rabbitmq_jms_topic_exchange",
    '--no-print-directory',
    'virgin-node-tmpdir',
    'start-background-broker',
    "DEPS_DIR=${properties['deps.dir']}",
    "RABBITMQ_NODENAME=${nodename}",
    "RABBITMQ_NODE_PORT=${node_port}",
    "RABBITMQ_CONFIG_FILE=${project.build.directory}/test-classes/${nodename}"
  ]
  break

case ~/^stop-test-broker-.*/:
  command = [
    properties['make.bin'],
    '-C', "${properties['deps.dir']}/rabbitmq_jms_topic_exchange",
    '--no-print-directory',
    'stop-node',
    "DEPS_DIR=${properties['deps.dir']}",
    "RABBITMQ_NODENAME=${nodename}"
  ]
  break
}

def pb = new ProcessBuilder(command)
pb.redirectErrorStream(true)

def process = pb.start()
process.waitFor()
if (process.exitValue() != 0) {
  println(process.in.text.trim())
  fail("Failed to manage broker '${nodename}' with command: ${command.join(' ')}")
}
