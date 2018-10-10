from jpype import *
import tempfile
import os

jars = "ejml-0.23.jar javax.json-api-1.0-sources.jar javax.json.jar joda-time-2.9-sources.jar joda-time.jar jollyday-0.4.9-sources.jar jollyday.jar protobuf.jar slf4j-api.jar slf4j-simple.jar stanford-corenlp-3.9.1.jar stanford-corenlp-3.9.1-javadoc.jar stanford-corenlp-3.9.1-models.jar stanford-corenlp-3.9.1-sources.jar xom-1.2.10-src.jar xom.jar".split()
classpath = os.pathsep.join((x for x in jars))
jpath = getDefaultJVMPath()

def parse(text):
    f = tempfile.NamedTemporaryFile(delete=False)
    startJVM(jpath, '-ae', '-Djava.class.path=%s' % (classpath));
    Main = JClass('edu.stanford.nlp.time.SUTimeMain')
    Main.main(["-o", f.name, "-i", text])
    shutdownJVM()
    content = f.read().decode('utf-8')
    f.close()
    os.unlink(f.name)
    return content


if __name__ == "__main__":
    print(parse("I will leave for the US next month."))
