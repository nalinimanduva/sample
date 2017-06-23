public class UploadAction extends ActionSupport {
  private File uploadedFile;
  // setter and getter for uploadedFile
   
  public String execute() {
    try {
      // File path and file name are hardcoded for illustration
      File fileToCreate = new File("filepath", "filename");
      // Copy temporary file content to this file
      FileUtils.copyFile(uploadedFile, fileToCreate);
      return "SUCCESS";
    } catch (Throwable e) {
      addActionError(e.getMessage());
      return "ERROR";
    }
  }
} 
