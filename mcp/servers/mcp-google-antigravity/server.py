import sys
import logging
from mcp.server.mcpserver import MCPServer
from google import genai
from pydantic import BaseModel
import os

# Initialize MCP server
mcp = MCPServer("Google Antigravity MCP")

# Set up logging
logging.basicConfig(level=logging.INFO, stream=sys.stderr, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("mcp-google-antigravity")

# Initialize Gemini Client
# It will automatically pick up GEMINI_API_KEY from environment variables
try:
    client = genai.Client()
    logger.info("Successfully initialized Google GenAI client.")
except Exception as e:
    logger.error(f"Failed to initialize Google GenAI client: {e}")
    client = None

class GenerateContentRequest(BaseModel):
    prompt: str
    model: str = "gemini-2.5-flash"

@mcp.tool()
def generate_content(request: GenerateContentRequest) -> str:
    """Generate content using Google Gemini models."""
    if client is None:
        return "Error: Google GenAI client is not initialized. Check your GEMINI_API_KEY."

    try:
        response = client.models.generate_content(
            model=request.model,
            contents=request.prompt,
        )
        return response.text
    except Exception as e:
        logger.error(f"Error generating content: {e}")
        return f"Error generating content: {str(e)}"

if __name__ == "__main__":
    logger.info("Starting Google Antigravity MCP Server...")
    mcp.run()
