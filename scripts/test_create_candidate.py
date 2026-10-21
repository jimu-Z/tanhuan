import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
spec=importlib.util.spec_from_file_location("candidate",Path(__file__).with_name("create_candidate.py"))
candidate=importlib.util.module_from_spec(spec);spec.loader.exec_module(candidate)
class CandidateTests(unittest.TestCase):
 def test_existing_release_is_never_overwritten(self):
  with tempfile.TemporaryDirectory() as d:
   root=Path(d);(root/'deploy/release').mkdir(parents=True)
   with self.assertRaises(FileExistsError):candidate.create_candidate(root,'release')
 def test_backend_failure_prevents_frontend_and_final_release(self):
  with tempfile.TemporaryDirectory() as d:
   root=Path(d);calls=[]
   def fail(command,cwd,log):
    calls.append(command);raise RuntimeError('simulated backend failure')
   with patch.object(candidate,'source_snapshot',return_value={}):
    with self.assertRaisesRegex(RuntimeError,'backend failure'):candidate.create_candidate(root,'release',runner=fail)
   self.assertEqual(len(calls),1);self.assertFalse((root/'deploy/release').exists())
   self.assertTrue((root/'deploy/.release-building/INCOMPLETE').exists())
 def test_invalid_id_cannot_escape_deploy_directory(self):
  with self.assertRaises(ValueError):candidate.create_candidate(Path.cwd(),'../outside')
 def test_literal_credential_in_jar_is_rejected(self):
  import zipfile
  with tempfile.TemporaryDirectory() as d:
   jar=Path(d)/'app.jar'
   with zipfile.ZipFile(jar,'w') as z:z.writestr('BOOT-INF/classes/application.yml','token:\n  secret: test-literal-secret\n')
   with self.assertRaisesRegex(RuntimeError,'credential'):candidate.assert_environment_config(jar)
if __name__=='__main__':unittest.main()
